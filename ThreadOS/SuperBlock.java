public class SuperBlock {
    public int totalBlocks; //the number of diskblocks
    public int totalInodes; //the number of iNodes
    public int freeList;    //the block number of the free list's head



    public SuperBlock(int diskSize) {
        byte[] superBlock = new byte[512];


        SysLib.rawread(0, superBlock);
        totalBlocks = SysLib.bytes2int(superBlock, 0);
        totalInodes = SysLib.bytes2int(superBlock, 4);
        freeList = SysLib.bytes2int(superBlock, 8);

        if(totalBlocks == diskSize && totalInodes > 0 && freeList >= 2) {
            return;
        } else {
            totalBlocks = diskSize;

        }


    }

    //sync basically writes totalBlocks, totalIndoes, and freeList to the disk.
    public void sync() {
        byte buffer = new byte[512];
        SysLib.int2bytes(totalBlocks, buffer, 0);
        SysLib.int2bytes(totalInodes, buffer, 4);
        SysLib.int2bytes(freeList, buffer, 8);
        SysLib.rawwrite(buffer, 0);

    }


    public int getFreeBlocks() {


    }

    public int returnBlock(int blockID) {

    }
}