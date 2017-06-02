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
            format(64);

        }


    }

    //sync function, basically writes totalBlocks, totalIndoes, and freeList to the disk.
    public void sync() {
        byte[] buffer = new byte[512];

        SysLib.int2bytes(totalBlocks, buffer, 0);
        SysLib.int2bytes(totalInodes, buffer, 4);
        SysLib.int2bytes(freeList, buffer, 8);

        SysLib.rawwrite(0, buffer);

    }

    //getBlocks basically returns the first free block from the free list. The top block is dequeued from the free list
    public int getFreeBlocks() {

        if(freeList > 0 && freeList < totalBlocks) {
            byte[] data = new byte[512];

            SysLib.rawread(freeList, data); //Reading the free list block
            int temp = freeList;

            freeList = SysLib.bytes2int(data, 0);

            return temp;
        }

        return -1;

    }


    //format function, basically clears the disk of all that data and all the instance variables are set to default values
    //and returned to the disk
    public void format(int numFiles)
    {
        totalInodes = numFiles;
        Inode empty = null;

        for(int i = 0; i < totalInodes; i++) {
            empty = new Inode();
            empty.flag = 0;
            empty.toDisk((short) i);

        }

        if(numFiles % 16 == 0) {
            freeList = numFiles / 16 + 1;
        } else {
            freeList = numFiles / 16 + 2;  //The 6th block is the start of the freelist.
        }

        byte [] tempData = null;

        for(int i = freeList; i < 1000 - 1; i++) {
            tempData = new byte[512];

            for(int j = 0; j < 512; j++) {
                tempData[j] = 0;
            }

            SysLib.int2bytes(i + 1, tempData, 0);
            SysLib.rawwrite(i, tempData);
        }

        tempData = new byte[512];
        for(int j = 0; j < 512; j++) {


            tempData[j] = 0;
        }
        SysLib.int2bytes(-1, tempData, 0);
        SysLib.rawwrite(1000 - 1, tempData);

        sync();


    }

    //returnBlock function, adds the new freed block to the front of free list based on its blockID
    public void returnBlock(int blockID) {
        byte[] data = new byte[512];

        SysLib.int2bytes(freeList, data, 0);
        SysLib.rawwrite(blockID, data);

        freeList = blockID;

    }


}