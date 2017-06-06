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

    private static final int SEEK_SET = 0;
    private static final int SEEK_CUR = 1;
    private static final int SEEK_END = 2;

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

        superblock.format(maxFiles);
        root = new Directory(superblock.totalInodes);
        fileTable = new FileTable(root);
        return true;
    }

    /**
     * Close the given file, commit all file transactions, unregister file from calling thread's file descriptor table
     *
     * @param fte FTE to close
     * @return 0 on success, -1 on failure
     */
    boolean close(FileTableEntry fte)
    {
        synchronized(fte)
        {
            if(--fte.count == 0)
            {
                return fileTable.ffree(fte);
            }

            return true;
        }


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
        FileTableEntry fte = fileTable.falloc(fileName, mode);
        if(mode.equals("w"))
        {
            if(!deallocAllBlocks(fte))
            {
                return null;
            }
        }
        return fte;
    }
    /**
     * Read up to buffer.length() bytes from the given file into the given buffer, starting at the seek pointer, ending at EOF if less than the buffer length.
     *
     * @param fte    FTE to read from
     * @param buffer Buffer to read to
     * @return Returns the number of read bytes or -1 on failure.
     */
    int read(FileTableEntry fte, byte[] buffer)
    {

        int bufferLength = buffer.length;
        int reads = 0;

        synchronized(fte)
        {

            int i = 0;
            int blockSize = 512;

            while(bufferLength > 0 && fte.seekPtr < fteSize(fte))
            {
                int block = fte.inode.findTargetBlock(fte.seekPtr);
                if(block == -1)
                {
                    break;
                }

                byte[] tempData = new byte[blockSize];
                SysLib.rawread(block, tempData);

                int offset = fte.seekPtr % blockSize;

                int blocksRemaining = blockSize - i;

                int numOfFilesLeft = fteSize(fte) - fte.seekPtr;

                if(blocksRemaining < numOfFilesLeft)
                {
                    i = blocksRemaining;
                }
                else
                {
                    i = numOfFilesLeft;
                }
                if(i > bufferLength)
                {
                    i = bufferLength;
                }

                System.arraycopy(tempData, offset, buffer, reads, i);
                fte.seekPtr += i;
                bufferLength -= i;
                reads += i;
            }

        }

        return reads;
    }

    /**
     * Write the contents of the given buffer to the given file, starting at the seek pointer. This operation may overwrite and/or append.
     *
     * @param fte    FTE to write to
     * @param buffer Buffer to write from
     * @return Returns the number of bytes written or -1 on failure.
     */
    int write(FileTableEntry fte, byte[] buffer)
    {
        int tempBuffer = buffer.length;

        if(fte == null || fte.mode.equals("r"))
        {
            return -1;
        }
        synchronized (fte)
        {
            int writes = writeToTheDisk(fte, buffer);
            if(fte.seekPtr > fte.inode.length)
            {
                fte.inode.length = fte.seekPtr;
            }
            fte.inode.toDisk(fte.iNumber);
            return writes;
        }


    }

    public int writeToTheDisk(FileTableEntry fte, byte[] buffer)
    {
        int tempBuffer = buffer.length;
        int writes = 0;
        int blockSize = Disk.blockSize;

        while(tempBuffer > 0)
        {
            int tempTarget = fte.inode.findTargetBlock(fte.seekPtr);

            if(tempTarget == -1)
            {
                short newTarget = (short) superblock.getFreeBlock();

                int testTarget = fte.inode.getIndexNumber(fte.seekPtr, newTarget);

                if(testTarget == -3)
                {
                    short freeBlock = (short) superblock.getFreeBlock();


                    if(!fte.inode.setBlock(freeBlock))
                    {
                        return -1;
                    }

                    if(fte.inode.getIndexNumber(fte.seekPtr, newTarget) != 0)
                    {
                        return -1;
                    }
                }
                else if(testTarget == -2 || testTarget == -1)
                {
                    return -1;
                }
                tempTarget = newTarget;
            }

            byte[] bufferTemp = new byte[blockSize];
            SysLib.rawread(tempTarget, bufferTemp);

            int tempPointer = fte.seekPtr % blockSize;
            int difference = blockSize - tempPointer;

            if(difference > tempBuffer)
            {
                System.arraycopy(buffer, writes, bufferTemp, tempPointer, tempBuffer);
                SysLib.rawwrite(tempTarget, bufferTemp);

                fte.seekPtr += tempBuffer;
                writes += tempBuffer;
                tempBuffer = 0;

            }
            else
            {
                System.arraycopy(buffer, writes, bufferTemp, tempPointer, difference);
                SysLib.rawwrite(tempTarget, bufferTemp);

                fte.seekPtr += difference;
                writes += difference;
                tempBuffer -= difference;
            }

        }

        return writes;

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
        synchronized(fte)
        {
            switch(origin)
            {
                case SEEK_SET:
                    fte.seekPtr = offset;
                case SEEK_CUR:
                    fte.seekPtr += offset;
                    break;
                case SEEK_END:
                    fte.seekPtr = fte.inode.length + offset;
                    break;
                default:
                    return -1;
            }

            if(fte.seekPtr < 0)
            {
                fte.seekPtr = 0;
            }

            if(fte.seekPtr > fte.inode.length)
            {
                fte.seekPtr = fte.inode.length;
            }
        }

        return fte.seekPtr;
    }

    /**
     * Delete the specified file. All blocks used by the file are freed. Open files may not be deleted.
     *
     * @param fileName File to delete
     * @return 0 on success, -1 if file is open
     */
    boolean delete(String fileName)
    {
        FileTableEntry fte = open(fileName, "w");

        if(root.ifree(fte.iNumber) && close(fte))
        {
            return true;
        }

        return false;
    }

    /**
     * @param fte FTE to get size of
     * @return the size in bytes of the given file
     */
    synchronized int fteSize(FileTableEntry fte)
    {
        synchronized(fte)
        {
            Inode inode = fte.inode;
            return inode.length;
        }
    }

    /**
     * Deallocate all blocks in the given FTE, setting their references to -1
     *
     * @param fte FTE to deallocate blocks in
     * @return Success! (or not)
     */
    private boolean deallocAllBlocks(FileTableEntry fte)
    {
        deallocate(fte);

        byte[] tempData = fte.inode.freeIndirectBlock();

        if(tempData != null)
        {
            returnAllBlocks(tempData);
        }
        fte.inode.toDisk(fte.iNumber);
        return true;
    }

    private void returnAllBlocks(byte[] data)
    {
        short blockID;
        while((blockID = SysLib.bytes2short(data, 0)) != -1)
        {
            superblock.returnBlock(blockID);
        }
    }

    private void deallocate(FileTableEntry files)
    {
        for(short blockID = 0; blockID < files.inode.DIRECT_SIZE; blockID++)
        {
            if(files.inode.direct[blockID] != -1)
            {
                superblock.returnBlock(blockID);
                files.inode.direct[blockID] = -1;
            }
        }
    }
}