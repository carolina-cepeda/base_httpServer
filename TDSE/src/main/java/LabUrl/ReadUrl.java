package LabUrl;
import java.io.*;
import java.net.*;


public class ReadUrl {
    public static void main(String[] args) throws Exception {
        URL google = new URI("http://ldbn.escuelaing.edu.co:5678/respuestasexamen.txt?year=2026").toURL();
        System.out.println("Protocol:" + google.getProtocol());
        System.out.println("Authority:" + google.getAuthority());
        System.out.println("Host:" + google.getHost());
        System.out.println("Port:" + google.getPort());
        System.out.println("Path:" + google.getPath());
        System.out.println("Query:" + google.getQuery());
        System.out.println("File:" + google.getFile());
        System.out.println("Path:" + google.getPath());
    }
}