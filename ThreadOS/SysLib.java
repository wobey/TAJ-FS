import java.util.*;

public class SysLib {
    public static int exec( String args[] ) {
        return Kernel.interrupt( Kernel.INTERRUPT_SOFTWARE,
				 Kernel.EXEC, 0, args );
    }

    public static int join( ) {
        return Kernel.interrupt( Kernel.INTERRUPT_SOFTWARE,
				 Kernel.WAIT, 0, null );
    }

    public static int boot( ) {
	return Kernel.interrupt( Kernel.INTERRUPT_SOFTWARE,
				 Kernel.BOOT, 0, null );
    }

    public static int exit( ) {
	return Kernel.interrupt( Kernel.INTERRUPT_SOFTWARE,
				 Kernel.EXIT, 0, null );
    }

    public static int sleep( int milliseconds ) {
	return Kernel.interrupt( Kernel.INTERRUPT_SOFTWARE,
				 Kernel.SLEEP, milliseconds, null );
    }

    public static int disk( ) {
	return Kernel.interrupt( Kernel.INTERRUPT_DISK,
				 0, 0, null );
    }

    public static int cin( StringBuffer s ) {
        return Kernel.interrupt( Kernel.INTERRUPT_SOFTWARE,
				 Kernel.READ, 0, s );
    }

    public static int cout( String s ) {
        return Kernel.interrupt( Kernel.INTERRUPT_SOFTWARE,
				 Kernel.WRITE, 1, s );
    }

    public static int cerr( String s ) {
        return Kernel.interrupt( Kernel.INTERRUPT_SOFTWARE,
				 Kernel.WRITE, 2, s );
    }

    public static int rawread( int blkNumber, byte[] b ) {
        return Kernel.interrupt( Kernel.INTERRUPT_SOFTWARE,
				 Kernel.RAWREAD, blkNumber, b );
    }

    public static int rawwrite( int blkNumber, byte[] b ) {
        return Kernel.interrupt( Kernel.INTERRUPT_SOFTWARE,
				 Kernel.RAWWRITE, blkNumber, b );
    }

    public static int sync( ) {
        return Kernel.interrupt( Kernel.INTERRUPT_SOFTWARE,
				 Kernel.SYNC, 0, null );
    }

    public static int cread( int blkNumber, byte[] b ) {
        return Kernel.interrupt( Kernel.INTERRUPT_SOFTWARE,
				 Kernel.CREAD, blkNumber, b );
    }

    public static int cwrite( int blkNumber, byte[] b ) {
        return Kernel.interrupt( Kernel.INTERRUPT_SOFTWARE,
				 Kernel.CWRITE, blkNumber, b );
    }

    public static int flush( ) {
        return Kernel.interrupt( Kernel.INTERRUPT_SOFTWARE,
				 Kernel.CFLUSH, 0, null );
    }

    public static int csync( ) {
        return Kernel.interrupt( Kernel.INTERRUPT_SOFTWARE,
				 Kernel.CSYNC, 0, null );
    }

    public static String[] stringToArgs( String s ) {
	StringTokenizer token = new StringTokenizer( s," " );
	String[] progArgs = new String[ token.countTokens( ) ];
	for ( int i = 0; token.hasMoreTokens( ); i++ ) {
	    progArgs[i] = token.nextToken( );
	}
	return progArgs;
    }

    public static void short2bytes( short s, byte[] b, int offset ) {
	b[offset] = (byte)( s >> 8 );
	b[offset + 1] = (byte)s;
    }

    public static short bytes2short( byte[] b, int offset ) {
	short s = 0;
        s += b[offset] & 0xff;
	s <<= 8;
        s += b[offset + 1] & 0xff;
	return s;
    }

    public static void int2bytes( int i, byte[] b, int offset ) {
	b[offset] = (byte)( i >> 24 );
	b[offset + 1] = (byte)( i >> 16 );
	b[offset + 2] = (byte)( i >> 8 );
	b[offset + 3] = (byte)i;
    }

    public static int bytes2int( byte[] b, int offset ) {
	int n = ((b[offset] & 0xff) << 24) + ((b[offset+1] & 0xff) << 16) +
	        ((b[offset+2] & 0xff) << 8) + (b[offset+3] & 0xff);
	return n;
    }
    
    /*
    Open the file in a given mode (r, w, w+, or a), set the seek pointer
    (0 for r/w/w+, EOF for a). If the file does not exist (under w, w+, or a), 
    it is created. If the file does not exist under append (a) mode, return -1.
    If the calling calling thread's file descriptor table is full, return -2.
    Otherwise, returns file descriptor for the file.
    */
    public int format(int maxFiles)
    {
        
    }
    
    /*
    Read up to buffer.length() bytes from the file into the buffer, starting at
    the seek pointer, ending at EOF if less than the buffer length. Increments the
    seek pointer by the number of bytes read. Returns the number of read bytes
    or -1 on failure.
    */
    public int read(int fileDescriptor, byte buffer[])
    {
        
    }
    
    /*
    Write the content of the buffer ot the file, starting at the seek pointer.
    This operation may overwrite and/or append. Increments the seek pointer by the
    number of bytes written. Returns the number of bytes written, or -1 on failure.
    */
    public int write(int fileDescriptor, byte buffer[])
    {
        
    }
    
    /*
    Offset can be pos or neg. Origin is within 0, 1, 2. Updates the seek pointer
    based on offset and origin combo:
    origin = 0: seek is moved to beginning of file + offset;
    origin = 1: seek is moved to current value + offset;
    origin = 2: seek is moved to size of file + offset.
    If origin and offset makes seek negative, seek pointer is set to 0.
    If origin and offset makes seek greater than EOF, the seek
    pointer is set to EOF.
    Return 0 for success, -1 on failure.
    */
    public int seek(int fileDescriptor, int offset, int origin)
    {
        
    }
    
    /*
    Closes file, commit all file transactions, unregister file from calling
    thread's descriptor table.
    Returns 0 for success, -1 on failure.
    */
    public int close(int fileDescriptor)
    {
        
    }
    
    /*
    Delete file and free all blocks used by file.
    Open files may not be deleted.
    Return 0 for success, -1 if file is open.
    */
    public int delete(String fileName)
    {
        
    }
    
    /*
    Return size in bytes of file.
    */
    public int fsize(int fileDescriptor)
    {
        
    }
}