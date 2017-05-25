public class Directory 
{
    private final static int FILENAME_MAX_CHARS = 30;
    private final static int TWICE_FILENAME_LENGTH = FILENAME_MAX_CHARS * 2;
    private final static int NUMBER_OF_INODE_BLOCKS = 64;
    private int fileSize[];
    private int offset;
    private char fileNames[][];
    
    public Directory(int maxFiles)
    {
        fileSize = new int[maxFiles];
        for (int i = 0; i < maxFiles; ++i)
            fileSize[i] = 0;
        offset = 0;
        fileNames = new char[maxFiles][FILENAME_MAX_CHARS];
        String root = "/";
        fileSize[0] = root.length();
        root.getChars(0, fileSize[0], fileNames[0], 0);
    }
    
    /*
    Initializes the Directory instance with this data[] (made with directoryToBytes())
    */
    public int bytes2Directory(byte data[])//bytesToDirectory(byte data[])
    {
        offset = 0;
        String tempFileName;
        int numberOfEntries = data.length / (NUMBER_OF_INODE_BLOCKS);
        
        // set file sizes
        for (int i = 0; i < fileSize.length; ++i)
        {
            fileSize[i] = SysLib.bytes2int(data, offset);
            offset += 4;
        }
        
        // set filenames
        for (int i = 0; i < fileSize.length; ++i)
        {
            
            tempFileName = new String(data, offset, TWICE_FILENAME_LENGTH);
            tempFileName.getChars(0, fileSize[i], fileNames[i], 0);
        }
        
        return numberOfEntries;
    }
    
    /*
    Converts and returns Directory information into a plain byte array
    this byte array will be written back to disk
    note: only meaningful directory info should be converted to bytes
    (only basic data).
    */
    public byte[] directory2Bytes()//directory2Bytes()
    {
        offset = 0;
        String tempFileName;
        byte [] byteArray = new byte[NUMBER_OF_INODE_BLOCKS];
        
        // store file sizes
        for (int i = 0; i < fileSize.length; ++i)
        {
            SysLib.int2bytes(fileSize[i], byteArray, offset);
            offset += 4;
        }
        
        // store filenames
        for (int i = 0; i < fileSize.length; ++i)
        {
            tempFileName = new String(fileNames[i], 0, fileSize[i]);
            System.arraycopy(tempFileName.getBytes(), 0, byteArray, offset, tempFileName.getBytes().length);
            offset += 4;
        }
        
        return byteArray;
    }
    
    /*
    Allocates a new inode number for this filename.
    */
    public short ialloc(String filename)//inodeAllocate(String filename)
    {
        
    }
    
    /*
    Deallocates this inumber (iNumber) and the
    corresponding file will be deleted.
    */
    public boolean ifree(short iNumber)//inodeFree(short iNumber)
    {
        
    }
    
    /*
    Returns the iNumber corresponding to this filename.
    */
    public short namei(String filename)//inodeOfNames(String filename)
    {
        
    }
}
