package tf.tuff.viablocks;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

public class ChunkDataHandler extends ChannelOutboundHandlerAdapter {

    private final CustomBlockListener blockListener;

    public ChunkDataHandler(CustomBlockListener blockListener) {
        this.blockListener = blockListener;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof ByteBuf) {
            ByteBuf buf = (ByteBuf) msg;
            
            if (buf.readableBytes() < 5) {
                super.write(ctx, msg, promise);
                return;
            }

            buf.markReaderIndex();
            boolean isChunkPacket = false;
            int skipCount = 0;

            try {
                int firstVarInt = read(buf);

                if (firstVarInt == 0x20) {
                    isChunkPacket = true;
                    skipCount = 1; 
                } 
                else if (firstVarInt > 0x7F && buf.readableBytes() > 1) { 
                    int secondVarInt = read(buf);
                    
                    if (secondVarInt == 0x20) {
                        isChunkPacket = true;
                        skipCount = 2; 
                    }
                }
            } catch (Exception e) {
            }

            buf.resetReaderIndex();

            if (isChunkPacket) {
                try {
                    for (int i = 0; i < skipCount; i++) read(buf);

                    int chunkX = buf.readInt();
                    int chunkZ = buf.readInt();
                    
                    buf.resetReaderIndex(); 

                    byte[] customData = blockListener.getCachedChunkData(chunkX, chunkZ);
                    if (customData != null && customData.length > 0) {
                        blockListener.plugin.plugin.getLogger().info("DEBUG: [FOUND] Chunk " + chunkX + "," + chunkZ + " | Injecting custom data!");
                        
                        ByteBuf newPacket = ctx.alloc().buffer(buf.readableBytes() + customData.length);
                        newPacket.writeBytes(buf);
                        newPacket.writeBytes(customData);
                        buf.release();
                        ctx.write(newPacket, promise);
                        return;
                    }
                } catch (Exception e) {
                    buf.resetReaderIndex();
                }
            }
        }
        super.write(ctx, msg, promise);
    }

    private int read(ByteBuf buf) {
        int numRead = 0;
        int result = 0;
        byte read;
        do {
            read = buf.readByte();
            int value = (read & 0b01111111);
            result |= (value << (7 * numRead));
            numRead++;
            if (numRead > 5) throw new RuntimeException("VarInt is too big");
        } while ((read & 0b10000000) != 0);
        return result;
    }
}