public class Directory 
{
    private final static int FILENAME_MAX_CHARS = 30;
    private int fileSize[];
    private char fileNames[][];
    
    public Directory(int maxFiles)
    {
        fileSize = new int[maxFiles];
        for (int i = 0; i < maxFiles; ++i)
            fileSize[i] = 0;
        fileNames = new char[maxFiles][FILENAME_MAX_CHARS];
        String root = "/";
        fileSize[0] = root.length();
        root.getChars(0, fileSize[0], fileNames[0], 0);
    }
    
    /*
    Initializes the Directory instance with this data[]
    */
    public int bytesToDirectory(byte data[])
    {
        
    }
    
    /*
    Converts and returns Directory information into a plain byte array
    this byte array will be written back to disk
    note: only meaningful directory info should be converted to bytes
    (only basic data).
    */
    public byte[] directoryToBytes()
    {
        
    }
    
    /*
    Allocates a new inode number for this filename.
    */
    public short inodeAllocate(String filename)
    {
        
    }
    
    /*
    Deallocates this inumber (iNumber) and the
    corresponding file will be deleted.
    */
    public boolean inodeFree(short iNumber)
    {
        
    }
    
    /*
    Returns the iNumber corresponding to this filename.
    */
    public short inodeOfNames(String filename)
    {
        
    }
}
