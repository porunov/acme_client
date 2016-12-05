package com.jblur.acme_client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.shredzone.acme4j.exception.AcmeException;
import org.shredzone.acme4j.util.CertificateUtils;
import org.shredzone.acme4j.util.KeyPairUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.cert.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class IOManager {

    private static final Logger LOG = LoggerFactory.getLogger(IOManager.class);

    private static final Gson gson = new GsonBuilder().create();

    public static KeyPair readKeyPairFromPrivateKey(String filePath) throws IOException {
        try (FileReader fr = new FileReader(filePath)) {
            return KeyPairUtils.readKeyPair(fr);
        }
    }

    public static KeyPair readKeyPairFromPrivateKey(File file) throws IOException {
        try (FileReader fr = new FileReader(file)) {
            return KeyPairUtils.readKeyPair(fr);
        }
    }

    public static void writeObject(Object obj, Class objClass, String path) throws IOException {
        try (Writer writer = new FileWriter(path)) {
            gson.toJson(obj, objClass, writer);
        }
    }

    public static Object readObject(String path, Class objClass) throws IOException {
        try (Reader reader = new FileReader(path)) {
            return gson.fromJson(reader, objClass);
        }
    }

    public static void createDirectories(String path) throws IOException {
        Files.createDirectories(Paths.get(path));
    }

    public static void write(byte[] data, String path) throws IOException {
        Files.write(Paths.get(path), data);
    }

    public static byte[] read(String path) throws IOException {
        return Files.readAllBytes(Paths.get(path));
    }

    public static void writeString(String path, String str) throws IOException {
        Files.write(Paths.get(path), Arrays.asList(str));
    }

    public static String readString(String path) throws IOException {
        return Files.readAllLines(Paths.get(path)).get(0);
    }

    public static boolean isDirectoryExists(String path) {
        return Files.isDirectory(Paths.get(path));
    }

    public static boolean isFileExists(String path) {
        File file = new File(path);
        return file.isFile() && file.exists();
    }

    public static byte[] readCSR(String path) throws IOException {
        return CertificateUtils.readCSR(new FileInputStream(path)).getEncoded();
    }

    public static void serialize(Object obj, String path) throws IOException {
        try (FileOutputStream fileOut = new FileOutputStream(path);
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(obj);
        }
    }

    public static Object deserialize(String path) throws IOException, ClassNotFoundException {
        try (FileInputStream fileIn = new FileInputStream(path);
             ObjectInputStream in = new ObjectInputStream(fileIn)) {
            return in.readObject();
        }
    }

    public static void writeX509Certificate(X509Certificate certificate, String path) throws IOException, CertificateEncodingException {
        try (Writer writer = new FileWriter(path); PemWriter pemWriter = new PemWriter(writer)) {
            pemWriter.writeObject(new PemObject("CERTIFICATE", certificate.getEncoded()));
        }
    }

    public static void writeX509CertificateChain(X509Certificate[] certificates, String path) throws IOException, CertificateEncodingException {
        try (Writer writer = new FileWriter(path); PemWriter pemWriter = new PemWriter(writer)) {
            for (X509Certificate certificate : certificates) {
                pemWriter.writeObject(new PemObject("CERTIFICATE", certificate.getEncoded()));
            }
        }
    }

    public static X509Certificate[] readX509Certificates(String path) throws IOException, CertificateException, AcmeException {
        try (FileInputStream fis = new FileInputStream(path)) {

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Collection c = cf.generateCertificates(fis);
            Iterator it = c.iterator();
            ArrayList<X509Certificate> certificates = new ArrayList<>();

            while (it.hasNext()) {
                Certificate cert = (Certificate) it.next();
                CertificateFactory certFact = CertificateFactory.getInstance("X.509");
                certificates.add((X509Certificate) certFact.generateCertificate(new ByteArrayInputStream(cert.getEncoded())));
            }

            X509Certificate[] x509Certificates = new X509Certificate[certificates.size()];

            for (int i = 0; i < x509Certificates.length; i++) {
                x509Certificates[i] = certificates.get(i);
            }

            return x509Certificates;
        }
    }
}
