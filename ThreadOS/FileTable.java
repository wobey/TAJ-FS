public class FileTable {

    private Vector table;
    private Directory dir;

    public FileTable(Directory directory) {
        table = new Vector();
        dir = directory;
    }

    public synchronized FileTableEntry fteAllocate(String filename, String node) {

    }

    public synchronized boolean fteFree (FileTableEntry e) {

    }

    public synchronized boolean fteEmpty() {
        return table.isEmpty();
    }
}