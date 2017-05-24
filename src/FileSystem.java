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
     */
    public FileSystem(int diskBlocks)
    {

    }

    /**
     * Format the disk with the given maximum number of file inodes
     *
     * @param maxFiles Maximum number of files
     * @return 0 on success, -1 on failure
     */
    public int format(int maxFiles)
    {

    }

    /**
     * Open the specified file in the given mode and set the seek pointer to the appropriate location (0 for "r"/"w"/"w+", EOF for "a").
     *
     * @param fileName File to open
     * @param mode     Mode to open in ("r": Read, "w": Write, "w+": Read/Write, "a": Append)
     * @return File descriptor on success, -1 if file does not exist in mode "a", -2 if calling thread's file descriptor table is full
     */
    public int open(String fileName, String mode)
    {

    }

    /**
     * Read up to buffer.length() bytes from the given file into the given buffer, starting at the seek pointer, ending at EOF if less than the buffer length.
     *
     * @param fileDescriptor File to read from
     * @param buffer         Buffer to read to
     * @return Number of bytes read on success, -1 on failure
     */
    public int read(int fileDescriptor, byte buffer[])
    {

    }

    /**
     * Write the contents of the given buffer to the given file, starting at the seek pointer. This operation may overwrite and/or append.
     *
     * @param fileDescriptor File to write to
     * @param buffer         Buffer to write from
     * @return Number of bytes written on success, -1 on failure
     */
    public int write(int fileDescriptor, byte buffer[])
    {

    }

    /**
     * Update the seek pointer based on the given offset and origin:
     * - If origin = 0: seek pointer is moved to the beginning of the file + offset
     * - If origin = 1: seek pointer is moved to its current value + offset
     * - If origin = 2: seek pointer is moved to the size of the file + offset
     * <p>
     * If the given origin and offset would make the seek pointer negative, the seek pointer is set to 0.
     * If the given origin and offset would make the seek pointer greater than EOF, the seek pointer is set to EOF.
     *
     * @param fileDescriptor File to move seek pointer for
     * @param offset         Offset to apply to seek
     * @param origin         Location to seek from
     * @return 0 on success, -1 on failure
     */
    public int seek(int fileDescriptor, int offset, int origin)
    {

    }

    /**
     * Close the given file, commit all file transactions, unregister file from calling thread's file descriptor table.
     *
     * @param fileDescriptor File to close
     * @return 0 on success, -1 on failure
     */
    public int close(int fileDescriptor)
    {

    }

    /**
     * Delete the specified file. All blocks used by the file are freed. Open files may not be deleted.
     *
     * @param fileName File to delete
     * @return 0 on success, -1 on failure
     */
    public int delete(String fileName)
    {

    }
}
