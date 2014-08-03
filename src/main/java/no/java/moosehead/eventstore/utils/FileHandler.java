package no.java.moosehead.eventstore.utils;

import java.io.*;

public class FileHandler {

    private final String ENCODING = "UTF-8";
    private boolean isInMemory = false;
    private String filename;
    private FileInputStream fileInputStream;
    private PrintWriter printWriter;

    public FileHandler(){
        isInMemory = true;
    }

    public FileHandler(String filename) {
        this.filename = filename;
    }

    public void writeToFile(String line) {
        if (!isInMemory) {
            printWriter.append(line);
            printWriter.flush();
        }
    }

    public InputStreamReader getInputStreamReader() {
        if (!isInMemory) {
            try {
                return new InputStreamReader(fileInputStream,ENCODING);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        } else {
            return  new InputStreamReader(new ByteArrayInputStream("".getBytes()));
        }
    }

    public void openFileForInput() {
        if (!isInMemory) {
            try {
                fileInputStream = new FileInputStream(filename);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void openFileForOutput() {
        if (!isInMemory) {
            try {
                printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(filename,true), ENCODING));
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void closeInputFile() {
        if (!isInMemory) {
            try {
                fileInputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
