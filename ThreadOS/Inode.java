public class Inode
{
    private final static int INODE_SIZE = 32;
    public final static int DIRECT_SIZE = 11;   // # direct pointers
    private final static int NUM_INODES_IN_BLOCK = 16;

    public int length;                          // file sinze in bytes
    public short count;                         // # file-table entries pointing to this
    // FLAGS: 0=unused, 1=used, 2=reading, 3=writing, -1=deletion
    public short flag;
    public short direct[] = new short[DIRECT_SIZE];  // direct pointers
    public short indirect;                      // an indirect pointer

    public Inode()
    {
        length = 0;
        count = 0;
        flag = 1;
        for (int i = 0; i < DIRECT_SIZE; ++i)
            direct[i] = -1;
        indirect = -1;
    }

    public Inode (short blockID)

    {

        int diskBlockID = 1 + (blockID / NUM_INODES_IN_BLOCK);
        byte[] tempData = new byte[Disk.blockSize];

        SysLib.rawread(diskBlockID, tempData);  // read Inode from disk
        int offset =  INODE_SIZE * (blockID % NUM_INODES_IN_BLOCK);

        length = SysLib.bytes2int(tempData, offset);
        offset += 4;
        count = SysLib.bytes2short(tempData, offset);
        offset += 2;
        flag = SysLib.bytes2short(tempData, offset);
        offset += 2;

        // store direct pointers
        for (int i = 0; i < DIRECT_SIZE; ++i)
        {
            direct[i] = SysLib.bytes2short(tempData, offset);
            offset += 2;
        }

        // store indirect pointer
        indirect = SysLib.bytes2short(tempData, offset);
        offset += 2;
    }



    public void toDisk(short blockID)
    {

        int diskBlockID = 1 + (blockID / NUM_INODES_IN_BLOCK);
        int offset =  0;
        byte[] tempData = new byte[Disk.blockSize];


        SysLib.int2bytes(length, tempData, offset);
        offset += 4;
        SysLib.short2bytes(count, tempData, offset);
        offset += 2;
        SysLib.short2bytes(flag, tempData, offset);
        offset += 2;

        //
        for (int i = 0; i < DIRECT_SIZE; ++i)
        {
            SysLib.short2bytes(direct[i], tempData, offset);
            offset += 2;
        }

        //
        SysLib.short2bytes(indirect, tempData, offset);
        offset += 2;

        byte[] newData = new byte[Disk.blockSize];
        SysLib.rawwrite(diskBlockID, newData);    // write to disk

        offset = (blockID % NUM_INODES_IN_BLOCK) * INODE_SIZE;

        System.arraycopy(tempData, 0, newData, offset, INODE_SIZE);
        SysLib.rawwrite(diskBlockID, newData);

        //return 0;   // success
    }

    int getIndexNumber (int fte, short offset)
    {
        int target = fte/Disk.blockSize;


        if(target < DIRECT_SIZE)
        {
            return setTarget(target, offset);
        }

        if(indirect < 0)
        {
            return -3;
        }
        else
        {
            return writeToDisk(target, offset);
        }

    }

    public int writeToDisk(int target, short offset)
    {
        byte[] tempData = new byte[Disk.blockSize];
        SysLib.rawread(indirect, tempData);

        int tempBlock = (target - DIRECT_SIZE) * 2;
        if(SysLib.bytes2short(tempData, tempBlock) > 0)
        {
            return -1;
        }
        else
        {
            SysLib.short2bytes(offset, tempData, tempBlock);
            SysLib.rawwrite(indirect, tempData);
            return 0;
        }
    }

    public int setTarget(int target, short offset)
    {
        if(direct[target] >= 0)
        {
            return -1;
        }

        if((target > 0) && (direct[target - 1] == -1))
        {
            return -2;
        }

        direct[target] = offset;
        return 0;
    }

    public boolean setBlock(short indexNumber)
    {
        for(int i = 0; i < DIRECT_SIZE; i++)
        {
            if(direct[i] == -1)
            {
                return false;
            }
        }

        if(indirect != -1)
        {
            return false;
        }

        indirect = indexNumber;
        byte[] tempData = new byte[Disk.blockSize];

        for(int i = 0; i < (Disk.blockSize/2); i++)
        {
            SysLib.short2bytes((short) -1, tempData, i * 2);

        }
        SysLib.rawwrite(indexNumber, tempData);
        return true;
    }

    int findTargetBlock(int offset)
    {
        int target = offset / Disk.blockSize;
        byte[] tempData = new byte[Disk.blockSize];

        if(target < DIRECT_SIZE)
        {
            return direct[target];

        }
        if(target < 0)
        {
            return -1;
        }

        int tempBlock = (target - DIRECT_SIZE) * 2;
        return SysLib.bytes2short(tempData, tempBlock);


    }

    byte[] freeIndirectBlock()
    {
        byte[] tempData = new byte[Disk.blockSize];
        if(indirect < 0)
        {
            return null;
        }
        SysLib.rawread(indirect, tempData);
        indirect = -1;
        return tempData;


    }


}

