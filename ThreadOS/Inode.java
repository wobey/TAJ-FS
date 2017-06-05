/* **********************************************************
// Title: Inode.java
// Author: John Fitzgerald
// Date: 5-28-17
// Description: 
** *********************************************************/
public class Inode 
{
    private final static int INODE_SIZE = 32;
    private final static int DIRECT_SIZE = 11;   // # direct pointers
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
    
    /*
    Retrieves inode from disk
    */
    public Inode (short blockID)
    {
        int offset =  INODE_SIZE * (blockID % NUM_INODES_IN_BLOCK);
        int diskBlockID = 1 + (blockID / NUM_INODES_IN_BLOCK);
        byte[] tempData = new byte[Disk.blockSize];
        
        SysLib.rawread(diskBlockID, tempData);  // read Inode from disk
        
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
    }
    
    /*
    Save to disk as the i-th inode
    Return 0 on success or -1 on failure
    */
    public void/*int*/ toDisk(short blockID)
    {
        int offset =  0;
        int diskBlockID = 1 + (blockID / NUM_INODES_IN_BLOCK);
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
        
        SysLib.rawwrite(diskBlockID, tempData);    // write to disk
        
        //return 0;   // success
    }
    
    /*
    Returns the target BlockID pointer (direct or indirect)
    */
    public int/*short*/ findTargetBlock(int offset)
    {
        int target = offset / Disk.blockSize;
        
        if (target < DIRECT_SIZE)    // direct
            return (direct[target] < 0) ? -1 : direct[target]; 
        else                         // indirect
            return getIndirectBlock(offset, target);
    }
    
    /*
    [findTargetBlock() helper function]
    Returns the target indirect BlockID pointer
    */
    public int/*short*/ getIndirectBlock(int offset, int target)
    {
        // handle IndexOutOfBoundsException
        if (offset >= length || indirect == -1)
            return -1;
        
        short indirectBlock;
        int blockWindow = 2 * (target - DIRECT_SIZE);
        byte[] tempData = new byte[Disk.blockSize];
        
        SysLib.rawread(indirect, tempData);
        indirectBlock = SysLib.bytes2short(tempData, blockWindow);
        
        return (indirectBlock == 0) ? -1 : indirectBlock;
    }
}