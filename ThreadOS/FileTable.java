import java.util.Vector;

public class FileTable {

    private Vector <FileTableEntry> table;
    private Directory dir;

    private final int READ = 2;
    private final int WRITE = 3;
    private final int DELETE = 4;
    private final int USED = 0;
    private final int UNUSED = -1;

    private enum flag{
        READ,
        WRITE
    }

    public FileTable(Directory directory) {
        table = new Vector<FileTableEntry>();
        dir = directory;
    }

    public synchronized FileTableEntry fteAllocate(String filename, String mode) {

        short iNumber = -1;
        Inode inode = null;

        boolean noRead = (mode.equals("w") || mode.equals("w+") || mode.equals("a")); //This has all the modes besides
        //the read mode.

        while(true) {
            iNumber = (filename.equals("/") ? (short) 0: dir.namei(filename));

            //If the iNode for the file exist then create a new Inode
            if(iNumber >= 0) {
                inode = new Inode(iNumber);

                //and its flag is read or used or unsed
                if(mode.equals("r")) {
                    if(inode.flag == READ || inode.flag == USED || inode.flag == UNUSED) {
                        break;

                        //else if the file is already been written by some other user, wait for it
                        //until it is done
                    } else if(inode.flag == WRITE) {
                        try {
                            wait();
                        } catch (InterruptedException e) {}

                    } else {
                        iNumber = -1;
                        return null;
                    }
                } else {
                    if (inode.flag == USED || inode.flag == UNUSED || inode.flag == WRITE) {
                        break;
                    } else {
                        iNumber = -1;
                        return null;

                    }
                }
            }
            else {
                //if the node for the particular file does not exist then
                //create a new iNode, and get the number from the directory
                //using the ialloc function
                if(noRead) {
                    iNumber = dir.ialloc(filename);
                    inode = new Inode(iNumber);
                    break;
                } else if(!noRead) {
                    return null;
                }
            }
        }
        inode.count++;
        inode.toDisk(iNumber);

        FileTableEntry fileEntry = new FileTableEntry(inode, iNumber, mode); //Creating a new FileTableEntry and adding it to
        // the file table
        table.addElement(fileEntry);

        return fileEntry;

    }

    public synchronized boolean fteFree (FileTableEntry e) {

        Inode inode = new Inode(e.iNumber);

        inode.count--;
        inode.toDisk(e.iNumber);

        return table.remove(e);

    }

    public synchronized boolean fteEmpty() {

        return table.isEmpty();
    }
}