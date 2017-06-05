//public class Inode
//{
//  public static final int iNodeSize = 32;
//  public static final int directSize = 11;
//  public static final int NoError = 0;
//  public static final int ErrorBlockRegistered = -1;
//  public static final int ErrorPrecBlockUnused = -2;
//  public static final int ErrorIndirectNull = -3;
//  public int length;
//  public short count;
//  public short flag;
//  public short[] direct = new short[11];
//  public short indirect;
//  
//  Inode() {
//    length = 0;
//    count = 0;
//    flag = 1;
//    for (int i = 0; i < 11; i++)
//      direct[i] = -1;
//    indirect = -1;
//  }
//  
//  Inode(short paramShort) {
//    int i = 1 + paramShort / 16;
//    byte[] arrayOfByte = new byte['Ȁ'];
//    SysLib.rawread(i, arrayOfByte);
//    int j = paramShort % 16 * 32;
//    
//    length = SysLib.bytes2int(arrayOfByte, j);
//    j += 4;
//    count = SysLib.bytes2short(arrayOfByte, j);
//    j += 2;
//    flag = SysLib.bytes2short(arrayOfByte, j);
//    j += 2;
//    for (int k = 0; k < 11; k++) {
//      direct[k] = SysLib.bytes2short(arrayOfByte, j);
//      j += 2;
//    }
//    indirect = SysLib.bytes2short(arrayOfByte, j);
//    j += 2;
//  }
//  
//
//
//
//
//
//
//
//
//  void toDisk(short paramShort)
//  {
//    byte[] arrayOfByte1 = new byte[32];
//    int i = 0;
//    
//    SysLib.int2bytes(length, arrayOfByte1, i);
//    i += 4;
//    SysLib.short2bytes(count, arrayOfByte1, i);
//    i += 2;
//    SysLib.short2bytes(flag, arrayOfByte1, i);
//    i += 2;
//    for (int j = 0; j < 11; j++) {
//      SysLib.short2bytes(direct[j], arrayOfByte1, i);
//      i += 2;
//    }
//    SysLib.short2bytes(indirect, arrayOfByte1, i);
//    i += 2;
//    
//    int j = 1 + paramShort / 16;
//    byte[] arrayOfByte2 = new byte['Ȁ'];
//    SysLib.rawread(j, arrayOfByte2);
//    i = paramShort % 16 * 32;
//    
//
//    System.arraycopy(arrayOfByte1, 0, arrayOfByte2, i, 32);
//    SysLib.rawwrite(j, arrayOfByte2);
//  }
//  
//
//
//
//
//
//
//
//
//  int findIndexBlock()
//  {
//    return indirect;
//  }
//  
//  boolean registerIndexBlock(short paramShort) {
//    for (int i = 0; i < 11; i++)
//      if (direct[i] == -1)
//        return false;
//    if (indirect != -1)
//      return false;
//    indirect = paramShort;
//    byte[] arrayOfByte = new byte['Ȁ'];
//    for (int j = 0; j < 256; j++)
//      SysLib.short2bytes((short)-1, arrayOfByte, j * 2);
//    SysLib.rawwrite(paramShort, arrayOfByte);
//    
//    return true;
//  }
//  
//  int findTargetBlock(int paramInt) {
//    int i = paramInt / 512;
//    if (i < 11) {
//      return direct[i];
//    }
//    if (indirect < 0) {
//      return -1;
//    }
//    byte[] arrayOfByte = new byte['Ȁ'];
//    SysLib.rawread(indirect, arrayOfByte);
//    int j = i - 11;
//    return SysLib.bytes2short(arrayOfByte, j * 2);
//  }
//  
//
//  int registerTargetBlock(int paramInt, short paramShort)
//  {
//    int i = paramInt / 512;
//    if (i < 11) {
//      if (direct[i] >= 0)
//        return -1;
//      if ((i > 0) && (direct[(i - 1)] == -1))
//        return -2;
//      direct[i] = paramShort;
//      return 0;
//    }
//    
//    if (indirect < 0) {
//      return -3;
//    }
//    byte[] arrayOfByte = new byte['Ȁ'];
//    SysLib.rawread(indirect, arrayOfByte);
//    int j = i - 11;
//    if (SysLib.bytes2short(arrayOfByte, j * 2) > 0) {
//      SysLib.cerr("indexBlock, indirectNumber = " + j + " contents = " + SysLib.bytes2short(arrayOfByte, j * 2) + "\n");
//      
//
//
//      return -1;
//    }
//    SysLib.short2bytes(paramShort, arrayOfByte, j * 2);
//    
//    SysLib.rawwrite(indirect, arrayOfByte);
//    return 0;
//  }
//  
//
//  byte[] unregisterIndexBlock()
//  {
//    if (indirect >= 0) {
//      byte[] arrayOfByte = new byte['Ȁ'];
//      SysLib.rawread(indirect, arrayOfByte);
//      indirect = -1;
//      return arrayOfByte;
//    }
//    
//    return null;
//  }
//}



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
        byte[] tempData = new byte[Disk.blockSize];
        int offset =  0;
        int diskBlockID = 1 + (blockID / NUM_INODES_IN_BLOCK);
        
        
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
        
        int doubleBlock = target - DIRECT_SIZE;
        
        short indirectBlock;
        int blockWindow = 2 * (doubleBlock);
        byte[] tempData = new byte[Disk.blockSize];
        
        SysLib.rawread(indirect, tempData);
        
        
        // TODO
        indirectBlock = SysLib.bytes2short(tempData, blockWindow);
        
        return (indirectBlock == 0) ? -1 : indirectBlock;
    }
    
    /*
    
    */
    public boolean setBlock(int offset, short target)
    {
        byte[] tempData = new byte[Disk.blockSize];
        short diskBlockID = (short)(offset/Disk.blockSize);
        
        // direct
        if (diskBlockID < direct.length)
        {
            if (direct[diskBlockID] > -1)
                return false;
            else
            {
                // direct found
                direct[diskBlockID] = target;
                return true;
            }
        }
        
        // indirect
        short doubleBlock = (short)(diskBlockID * 2);
        diskBlockID = (short)(diskBlockID - DIRECT_SIZE);
        SysLib.rawread(indirect, tempData);
        
        // TODO double diskBlockID
        if(SysLib.bytes2short(tempDatajavac , doubleBlock) == -1)
        {
            SysLib.short2bytes(target, tempData, doubleBlock);
            SysLib.rawwrite(indirect, tempData);
            
            // indirect found
            return true;
        }   
        
        // no valid block found
        return false;
    }
}