/**
 * FileSystem, part of TAJ-FS
 * -----------------------------------------------------------------------------
 * Created by Timothy Elmer on 5/23/2017.
 * -----------------------------------------------------------------------------
 * Purpose: Master class for TAJ-FS. Describes a Unix-like file system for ThreadOS.
 * -----------------------------------------------------------------------------
 * Notes:
 * -----------------------------------------------------------------------------
 */
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
     * Idunno what the fuck this does.
     *
     * @param fte
     * @return
     */
    private boolean deallocateAllBlocks(FileTableEntry fte)
    {

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
        root = new Directory(superblock.inodeBlocks);

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
            // root.bytesToDirectory(rootData); TODO REMOVE COMPAT
        }
        close(rootFTE);
    }

    /**
     *
     */
    void sync()
    {

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

        root = new Directory(maxFiles);

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
        return fileTable.falloc(fileName, mode);
        // return fileTable.fteAlloc(fileName, mode); TODO REMOVE COMPAT
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
        // Check args
        if (!(fte.mode.equals("r") || fte.mode.equals("w+")) || fte == null || buffer == null)
            return -1;


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
        // Check args
        if (!(fte.mode.equals("w") || fte.mode.equals("w+") || fte.mode.equals("a")) || fte == null || buffer == null)
            return -1;
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

    }

    /**
     * Delete the specified file. All blocks used by the file are freed. Open files may not be deleted.
     *
     * @param fileName File to delete
     * @return 0 on success, -1 if file is open
     */
    boolean delete(String fileName)
    {

    }

    /**
     * @param fte FTE to get size of
     * @return the size in bytes of the given file
     */
    int fteSize(FileTableEntry fte)
    {

    }
}
