// TODO: move to tuffx

package tf.tuff.viablocks;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import org.bukkit.entity.Player;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import java.util.ArrayList;
import java.util.List;

public class ChunkDataHandler extends ChannelOutboundHandlerAdapter {

    private final CustomBlockListener blockListener;
    private final Player player;

    public ChunkDataHandler(CustomBlockListener blockListener, Player player) {
        this.blockListener = blockListener;
        this.player = player;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof ByteBuf) {
            ByteBuf buf = (ByteBuf) msg;
            buf.markReaderIndex();
            
            try {
                int packetId = readVarInt(buf);
                byte[] extraData = null;
                
                if (packetId == 0x20) {
                    int x = buf.readInt();
                    int z = buf.readInt();
                    
                    extraData = blockListener.getExtraDataForChunk(player.getWorld().getName(), x, z); 
                } else if (packetId == 0x0B) {
                    int currentIndex = buf.readerIndex();
                    long val = buf.getLong(currentIndex); 
                    
                    int x = (int) (val >> 38);
                    int y = (int) ((val >> 26) & 0xFFF);
                    int z = (int) (val << 38 >> 38);
                    if (x >= 33554432) x -= 67108864;
                    if (y >= 2048) y -= 4096;
                    if (z >= 33554432) z -= 67108864;

                    long tuffPacked = blockListener.packLocation(x, y, z);
                    
                    Integer modernId = blockListener.getRecentChange(tuffPacked);

                    if (modernId != null) {
                        ByteArrayDataOutput out = ByteStreams.newDataOutput();
                        out.writeUTF("ADD_SINGLE");
                        out.writeInt(modernId);
                        out.writeLong(tuffPacked);
                        extraData = out.toByteArray();
                    }
                } else if (packetId == 0x10) { 
                    int chunkX = buf.readInt();
                    int chunkZ = buf.readInt();
                    
                    if (Math.abs(chunkX) < 2000000 && Math.abs(chunkZ) < 2000000) {
                        int recordCount = readVarInt(buf);
                        List<Long> locations = new ArrayList<>();
                        for (int i = 0; i < recordCount; i++) {
                            short horiz = buf.readUnsignedByte();
                            int y = buf.readUnsignedByte();
                            readVarInt(buf); 
                            
                            int x = (horiz >> 4 & 15) + (chunkX * 16);
                            int z = (horiz & 15) + (chunkZ * 16);
                            locations.add(blockListener.packLocation(x, y, z));
                        }
                        extraData = blockListener.getExtraDataForMultiBlock(player.getWorld(), locations);
                    }
                }

                if (extraData != null && extraData.length > 0) {
                    buf.resetReaderIndex(); 
                    
                    ByteBuf tail = ctx.alloc().buffer();
                    tail.writeBytes(extraData);
                    
                    CompositeByteBuf composite = ctx.alloc().compositeBuffer();
                    composite.addComponents(true, buf.retain(), tail);
                    
                    super.write(ctx, composite, promise);
                    return;
                }
            } catch (Exception e) {
            } finally {
                 if (buf.refCnt() > 0 && msg == buf) {
                     buf.resetReaderIndex();
                 }
            }
        }
        super.write(ctx, msg, promise);
    }

    private void writeVarInt(ByteBuf buf, int value) {
        while ((value & -128) != 0) {
            buf.writeByte(value & 127 | 128);
            value >>>= 7;
        }
        buf.writeByte(value);
    }

    private int readVarInt(ByteBuf buf) {
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