public class Inode 
{
    private final static int INODE_SIZE = 32;
    private final static int DIRECT_SIZE = 11;   // # direct pointers
    
    public int length;                          // file sinze in bytes
    public short count;                         // # file-table entries pointing to this
    public short flag;                          // 0=unused, 1=used...
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
        // TODO
    }
    
    /*
    Save to disk as the i-th inode
    Return 0 on success or -1 on failure
    */
    public int toDisk(short blockID)
    {
        
    }
    
    /*
    
    */
    public short getIndexBlockNumber()
    {
        
    }
    
    /*
    
    */
    public boolean setIndexBlock(short indexBlockNumber)
    {
        
    }
    
    /*
    
    */
    public short findTargetBlock(int offset)
    {
        
    }
}