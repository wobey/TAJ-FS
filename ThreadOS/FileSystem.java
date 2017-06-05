///**
// * FileSystem, part of TAJ-FS
// * -----------------------------------------------------------------------------
// * Created by Timothy Elmer on 5/23/2017.
// * -----------------------------------------------------------------------------
// * Purpose: Master class for TAJ-FS. Describes a Unix-like file system for ThreadOS.
// * -----------------------------------------------------------------------------
// * Notes:
// * -----------------------------------------------------------------------------
// */
public class FileSystem
{
    /**
     * The SuperBlock for this FileSystem
     */
    private SuperBlock superblock;

    /**
     * The root Directory for this FileSystem
     */
    private Directory root;

    /**
     * The FileTable for this FileSystem
     */
    private FileTable fileTable;

    /**
     * Deallocate all blocks in the given FTE, setting their references to -1
     *
     * @param fte FTE to deallocate blocks in
     * @return Success! (or not)
     */
    private boolean deallocateAllBlocks(FileTableEntry fte)
    {
        // Deallocate directs
        for (int i = 0; i < fte.inode.direct.length; i++)
{            
    fte.inode.direct[i] = -1;
superblock.returnBlock(i);
}

  fte.inode.toDIsk(fte.iNumber); 
  return true;
// NOT DO NOT DO  BELOW BELOW
//        if (fte.inode.indirect < 0)
//            return true;
//
//        byte[] indirect = new byte[Disk.blockSize];
//
//        if (SysLib.rawread(fte.inode.indirect, indirect) <= 0)
//            return false;
//
//        for (int i = 0; i < indirect.length; i++)
//            indirect[i] = -1;
//
//        return SysLib.rawwrite(fte.inode.indirect, indirect) > 0;
    }

    /**
     * Construct a FileSystem with the given number of blocks
     *
     * @param diskBlocks Number of blocks to use for this FileSystem
     * @throws IllegalArgumentException on number of blocks < 1
     */
    public FileSystem(int diskBlocks)
    {
        if (diskBlocks < 1)
            throw new IllegalArgumentException("A file system cannot be created with less than one block");

        // Create and format superblock
        superblock = new SuperBlock(diskBlocks);

        // Create directory and register root
        root = new Directory(superblock.totalInodes);

        // Create file table and store root
        fileTable = new FileTable(root);

        // Reconstruct root from disk
        FileTableEntry rootFTE = open("/", "r");
        int rootSize = fteSize(rootFTE);
        if (rootSize > 0)
        {
            byte[] rootData = new byte[rootSize];
            read(rootFTE, rootData);
            root.bytes2directory(rootData);
        }
        close(rootFTE);
    }

    /**
     * Synchronize file system to disk
     */
    void sync()
    {
        FileTableEntry dRoot = open("/", "w");
        byte[] buffer = root.directory2bytes();

        write(dRoot, buffer);
        close(dRoot);
        superblock.sync();
    }

    /**
     * Format the disk with the given maximum number of file inodes.
     *
     * @param maxFiles Maximum number of files
     * @return Return success
     */
    boolean format(int maxFiles)
    {
        if (maxFiles < 0)
            return false;

        while (!fileTable.fteEmpty());

        superblock.format(maxFiles);
        root = new Directory(maxFiles);
        fileTable = new FileTable(root);

        return true;
    }

    /**
     * Open the specified file in the given mode and set the seek pointer to the appropriate location (0 for "r"/"w"/"w+", EOF for "a").
     * If the file does not exist under "w"/"w+"/"a" modes, create it.
     *
     * @param fileName File to open
     * @param mode     Mode to open file in ("r": Read, "w": Write, "w+": Read/Write, "a": Append)
     * @return The created FTE on success, null on failure.
     */
    FileTableEntry open(String fileName, String mode)
    {
        return fileTable.fteAllocate(fileName, mode);
    }
    
    /**
     * Read up to buffer.length() bytes from the given file into the given buffer, starting at the seek pointer, ending at EOF if less than the buffer length.
     *
     * @param fte    FTE to read from
     * @param buffer Buffer to read to
     * @return Returns the number of read bytes or -1 on failure.
     */
    synchronized int read(FileTableEntry fte, byte[] buffer)
    {
        // Check args
        if (!(fte.mode.equals("r") || fte.mode.equals("w+")) || fte == null || buffer == null)
            return -1;

        int reads = 0;

        int offset = fte.seekPtr % Disk.blockSize;

        int first = 1;

        for (int b = 0; b < buffer.length / Disk.blockSize; b++)
        {
            // TODO is first=1 and then first=0 necessary?
            byte[] iBuffer = new byte[Disk.blockSize - offset * first];
            int target = fte.inode.findTargetBlock(fte.seekPtr);
            if (SysLib.rawread(target, iBuffer) < 0)
                return -1;

            int smaller;
            if (Disk.blockSize < buffer.length)
                smaller = (Disk.blockSize - offset);
            else
                smaller = (buffer.length - offset);
            
            // Changed 0 to offset in arrayCopy
            //System.arraycopy(iBuffer, 0, buffer, reads, iBuffer.length);
            System.arraycopy(iBuffer, offset, buffer, reads, iBuffer.length);

            fte.seekPtr += iBuffer.length;
            reads += iBuffer.length;

            first = 0;
        }

        byte[] iBuffer = new byte[(buffer.length - offset) % Disk.blockSize];

        if (SysLib.rawread(fte.inode.findTargetBlock(fte.seekPtr), iBuffer) < 0)
            return -1;

        System.arraycopy(iBuffer, 0, buffer, reads, iBuffer.length);

        fte.seekPtr += iBuffer.length;
        reads += iBuffer.length;

        return reads;
    }
    
    /**
     * Write the contents of the given buffer to the given file, starting at the seek pointer. This operation may overwrite and/or append.
     *
     * @param fte    FTE to write to
     * @param buffer Buffer to write from
     * @return Returns the number of bytes written or -1 on failure.
     */
    synchronized int write(FileTableEntry fte, byte[] buffer)
    {
        // Check args
        if (!(fte.mode.equals("w") || fte.mode.equals("w+") || fte.mode.equals("a")) || fte == null || buffer == null)
            return -1;

        int bufferSeek = 0; 
        int writeCount = 0;
        int block;
        byte[] iBuffer = new byte[Disk.blockSize];

        while (bufferSeek < buffer.length)
        {
            int writes = 0;

            block =  fte.inode.findTargetBlock(fte.seekPtr);

            if (block == 0)
            {
                block = superblock.getFreeBlock();
                fte.
            }
            
            for (int i = 0; i < iBuffer.length && bufferSeek < buffer.length; i++, bufferSeek++, writes++)
                iBuffer[i] = buffer[bufferSeek];

            if(SysLib.rawwrite(block, iBuffer) < 0)
                return -1;
            else    // Byte read
            {
                // Increase write counter and seek pointer by number of bytes written
                writeCount += writes;
                fte.seekPtr += writes;
            }
        }

        return writeCount;
    }

    /**
     * Offset may be positive or negative. Origin must be within {0, 1, 2}.
     * Update the seek pointer based on the given offset and origin:
     * - If origin = 0: seek pointer is moved to the beginning of the file + offset
     * - If origin = 1: seek pointer is moved to its current value + offset
     * - If origin = 2: seek pointer is moved to the size of the file + offset
     * <p>
     * If the given origin and offset would make the seek pointer negative, the seek pointer is set to 0
     * If the given origin and offset would make the seek pointer greater than EOF, the seek pointer is set to EOF
     *
     * @param fte    FTE to set pointer of
     * @param offset Offset to apply to pointer
     * @param origin Direction to apply offset from
     * @return 0 on success, -1 on failure
     */
    int seek(FileTableEntry fte, int offset, int origin)
    {
        if (fte.mode.equals("a"))
            return -1;

        int seekPointer = fte.seekPtr, length = fte.inode.length;

        // Check args
        if (fte == null || Math.abs(offset) > length || !(origin >= 0 && origin <= 2))
            return -1;


        // Clamp pointer to 0 if lesser
        if ((origin == 0 && offset < 0) || (origin == 1 && seekPointer + offset < 0) || (origin == 2 && length + offset < 0))
        {
            fte.seekPtr = 0;
            return 0;
        }

        // Clamp pointer to length - 1 if greater
        if ((origin == 0 && offset > length - 1) || (origin == 1 && seekPointer + offset > length - 1) || (origin == 2 && length + offset > length - 1))
        {
            fte.seekPtr = length - 1;
            return 0;
        }

        // Seek
        switch (origin)
        {
            case 0:
                fte.seekPtr = offset;
                break;
            case 1:
                fte.seekPtr += offset;
                break;
            case 2:
                fte.seekPtr = length - offset;
                break;
        }
        return 0;
    }

    /**
     * Close the given file, commit all file transactions, unregister file from calling thread's file descriptor table
     *
     * @param fte FTE to close
     * @return 0 on success, -1 on failure
     */
    boolean close(FileTableEntry fte)
    {
        return fileTable.fteFree(fte);
    }

    /**
     * Delete the specified file. All blocks used by the file are freed. Open files may not be deleted.
     *
     * @param fileName File to delete
     * @return 0 on success, -1 if file is open
     */
    boolean delete(String fileName)
    {
        // Check out file
        FileTableEntry fte = fileTable.fteAllocate(fileName, "r");

        // If file not checked out, or other threads own, fail
        if (fte == null || fte.count > 1)
        {
            fileTable.fteFree(fte);
            return false;
        }

        // Mark as unused
        fte.inode.flag = 0;

        // Free all direct blocks
        for (int i = 0; i < fte.inode.direct.length; i++)
            fte.inode.direct[i] = -1;

        if (fte.inode.indirect < 0)
            return true;

        byte[] indirect = new byte[Disk.blockSize];

        if (SysLib.rawread(fte.inode.indirect, indirect) <= 0)
            return false;

        for (int i = 0; i < indirect.length; i++)
            indirect[i] = -1;

        return SysLib.rawwrite(fte.inode.indirect, indirect) > 0;
    }

    /**
     * @param fte FTE to get size of
     * @return the size in bytes of the given file
     */
    int fteSize(FileTableEntry fte)
    {
        return fte.inode.length;
    }
}












//
//public class FileSystem
//{
//  private SuperBlock superblock;
//  private Directory directory;
//  private FileTable filetable;
//  
//  public FileSystem(int paramInt) {
//    superblock = new SuperBlock(paramInt);
//    
//
//    directory = new Directory(superblock.totalInodes);
//    
//
//    filetable = new FileTable(directory);
//    
//
//    FileTableEntry localFileTableEntry = open("/", "r");
//    int i = fsize(localFileTableEntry);
//    if (i > 0) {
//      byte[] arrayOfByte = new byte[i];
//      read(localFileTableEntry, arrayOfByte);
//      directory.bytes2directory(arrayOfByte);
//    }
//    close(localFileTableEntry);
//  }
//  
//  void sync()
//  {
//    FileTableEntry localFileTableEntry = open("/", "w");
//    byte[] arrayOfByte = directory.directory2bytes();
//    write(localFileTableEntry, arrayOfByte);
//    close(localFileTableEntry);
//    
//
//    superblock.sync();
//  }
//  
//  boolean format(int paramInt)
//  {
//    while (!filetable.fteEmpty()) {}
//    
//
//
//    superblock.format(paramInt);
//    
//
//    directory = new Directory(superblock.totalInodes);
//    
//
//    filetable = new FileTable(directory);
//    
//    return true;
//  }
//  
//  FileTableEntry open(String paramString1, String paramString2)
//  {
//    FileTableEntry localFileTableEntry = filetable.fteAllocate(paramString2, paramString2);
//    if ((paramString2 == "w") && 
//      (!deallocAllBlocks(localFileTableEntry)))
//      return null;
//    return localFileTableEntry;
//  }
//  
//  boolean close(FileTableEntry paramFileTableEntry)
//  {
//    synchronized (paramFileTableEntry)
//    {
//      paramFileTableEntry.count -= 1;
//      if (paramFileTableEntry.count > 0)
//        return true;
//    }
//    return filetable.fteFree(paramFileTableEntry);
//  }
//  
//  int fsize(FileTableEntry paramFileTableEntry) {
//    synchronized (paramFileTableEntry) {
//      return paramFileTableEntry.inode.length;
//    }
//  }
//  
// //    int read(FileTableEntry fte, byte[] buffer)
////    {
////        // Check args
////        if (!(fte.mode.equals("r") || fte.mode.equals("w+")) || fte == null || buffer == null)
////            return -1;
////
////        int reads = 0;
////
////        int offset = fte.seekPtr % Disk.blockSize;
////
////        int first = 1;
////
////        for (int b = 0; b < buffer.length / Disk.blockSize; b++)
////        {
////            byte[] iBuffer = new byte[Disk.blockSize - offset * first];
////            if (SysLib.rawread(fte.inode.findTargetBlock(fte.seekPtr), iBuffer) < 0)
////                return -1;
////
////            System.arraycopy(iBuffer, 0, buffer, reads, iBuffer.length);
////
////            fte.seekPtr += iBuffer.length;
////            reads += iBuffer.length;
////
////            first = 0;
////        }
////
////        byte[] iBuffer = new byte[(buffer.length - offset) % Disk.blockSize];
////
////        if (SysLib.rawread(fte.inode.findTargetBlock(fte.seekPtr), iBuffer) < 0)
////            return -1;
////
////        System.arraycopy(iBuffer, 0, buffer, reads, iBuffer.length);
////
////        fte.seekPtr += iBuffer.length;
////        reads += iBuffer.length;
////
////        return reads;
////    } 
//  
//  synchronized int read(FileTableEntry paramFileTableEntry, byte[] paramArrayOfByte) {
//    if ((paramFileTableEntry.mode == "w") || (paramFileTableEntry.mode == "a")) {
//      return -1;
//    }
//    int i = 0;
//    int j = paramArrayOfByte.length;
//    
//    synchronized (paramFileTableEntry) {
//      while ((j > 0) && (paramFileTableEntry.seekPtr < fsize(paramFileTableEntry)))
//      {
//
//
//        int k = paramFileTableEntry.inode.findTargetBlock(paramFileTableEntry.seekPtr);
//        if (k == -1) {
//          break;
//        }
//        byte[] arrayOfByte = new byte['Ȁ'];
//        SysLib.rawread(k, arrayOfByte);
//        
//
//        int m = paramFileTableEntry.seekPtr % 512;
//        
//
//        int n = 512 - m;
//        int i1 = fsize(paramFileTableEntry) - paramFileTableEntry.seekPtr;
//        
//        int i2 = Math.min(Math.min(n, j), i1);
//        //int readInto = (buffer.length > Disk.blockSize ? Disk.blockSize: buffer.length) - intraBlockOffset;
//        
//        System.arraycopy(arrayOfByte, m, paramArrayOfByte, i, i2);
//        
//
//        paramFileTableEntry.seekPtr += i2;
//        i += i2;
//        j -= i2;
//      }
//      return i;
//    }
//  }
//  
//  synchronized int write(FileTableEntry paramFileTableEntry, byte[] paramArrayOfByte)
//  {
//    if (paramFileTableEntry.mode == "r") {
//      return -1;
//    }
//    synchronized (paramFileTableEntry) {
//      int i = 0;
//      int j = paramArrayOfByte.length;
//      
//      while (j > 0)
//      {
//        int k = paramFileTableEntry.inode.findTargetBlock(paramFileTableEntry.seekPtr);
//        if (k == -1) 
//        {
//          short m = (short)superblock.getFreeBlock();
//          
//          paramFileTableEntry.inode.registerTargetBlock(paramFileTableEntry.seekPtr, m);
////          switch (paramFileTableEntry.inode.registerTargetBlock(paramFileTableEntry.seekPtr, m)) 
////          {
////          case 0: 
////            break;
////          case -2: 
////          case -1: 
////            SysLib.cerr("ThreadOS: filesystem panic on write\n");
////            return -1;
////          case -3: 
////            short s = (short)superblock.getFreeBlocks();
////            if (!paramFileTableEntry.inode.registerIndexBlock(s))
////            {
////              SysLib.cerr("ThreadOS: panic on write\n");
////              return -1;
////            }
////            if (paramFileTableEntry.inode.registerTargetBlock(paramFileTableEntry.seekPtr, m) != 0)
////            {
////
////              SysLib.cerr("ThreadOS: panic on write\n");
////              return -1;
////            }
////            break; }
//          k = m;
//        }
//        
//        byte[] arrayOfByte = new byte['Ȁ'];
//        
//
//        SysLib.rawread(k, arrayOfByte);
////        if (SysLib.rawread(k, arrayOfByte) == -1) {
////          System.exit(2);
////        }
//
//        int intraBlockOffset = paramFileTableEntry.seekPtr % 512;
//        int i1 = 512 - intraBlockOffset;
//        
//        int i2 = Math.min(i1, j);
//        
//        System.arraycopy(paramArrayOfByte, i, arrayOfByte, intraBlockOffset, i2);
//        
//        SysLib.rawwrite(k, arrayOfByte);
//        
//        
//        
//        
//        paramFileTableEntry.seekPtr += i2;
//        i += i2;
//        j -= i2;
//        
//
//        if (paramFileTableEntry.seekPtr > paramFileTableEntry.inode.length) {
//          paramFileTableEntry.inode.length = paramFileTableEntry.seekPtr;
//        }
//      }
//      //paramFileTableEntry.inode.toDisk(paramFileTableEntry.iNumber);
//      
//      return i;
//    }
//  }
//  
//  private boolean deallocAllBlocks(FileTableEntry paramFileTableEntry)
//  {
//    if (paramFileTableEntry.inode.count != 1) {
//      return false;
//    }
//    byte[] arrayOfByte = paramFileTableEntry.inode.unregisterIndexBlock();
//    if (arrayOfByte != null) {
//      int i = 0;
//      int j;
//      while ((j = SysLib.bytes2short(arrayOfByte, i)) != -1) {
//        superblock.returnBlock(j);
//      }
//    }
//    for (int i = 0; i < 11; i++)
//      if (paramFileTableEntry.inode.direct[i] != -1) {
//        superblock.returnBlock(paramFileTableEntry.inode.direct[i]);
//        paramFileTableEntry.inode.direct[i] = -1;
//      }
//    paramFileTableEntry.inode.toDisk(paramFileTableEntry.iNumber);
//    return true;
//  }
//  
//  boolean delete(String paramString) {
//    FileTableEntry localFileTableEntry = open(paramString, "w");
//    short s = localFileTableEntry.iNumber;
//    return (close(localFileTableEntry)) && (directory.ifree(s));
//  }
//  
//  private final int SEEK_SET = 0;
//  private final int SEEK_CUR = 1;
//  private final int SEEK_END = 2;
//  
//  int seek(FileTableEntry paramFileTableEntry, int paramInt1, int paramInt2) {
//    synchronized (paramFileTableEntry)
//    {
//
//
//
//
//
//      switch (paramInt2) {
//      case 0: 
//        if ((paramInt1 >= 0) && (paramInt1 <= fsize(paramFileTableEntry))) {
//          paramFileTableEntry.seekPtr = paramInt1;
//        } else
//          return -1;
//        break;
//      case 1: 
//        if ((paramFileTableEntry.seekPtr + paramInt1 >= 0) && (paramFileTableEntry.seekPtr + paramInt1 <= fsize(paramFileTableEntry)))
//        {
//          paramFileTableEntry.seekPtr += paramInt1;
//        } else
//          return -1;
//        break;
//      case 2: 
//        if ((fsize(paramFileTableEntry) + paramInt1 >= 0) && (fsize(paramFileTableEntry) + paramInt1 <= fsize(paramFileTableEntry)))
//        {
//          paramFileTableEntry.seekPtr = (fsize(paramFileTableEntry) + paramInt1);
//        } else
//          return -1;
//        break;
//      }
//      return paramFileTableEntry.seekPtr;
//    }
//  }
//}