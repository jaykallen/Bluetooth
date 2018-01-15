package com.procatdt.appbluetooth;

// Created by Jay Kallen on 11/6/2017:  This creates a ZPL file with embedded image and prints it to a Zebra QLN420 bluetooth printer

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Pair;

import com.zebra.sdk.comm.BluetoothConnectionInsecure;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.graphics.ZebraImageFactory;
import com.zebra.sdk.graphics.ZebraImageI;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import timber.log.Timber;

public class PrinterManager {
    public static Observable<String> printTest (final String mac, final String body) {
        return Observable.fromCallable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                try {
                    Timber.d("Printing to: " + mac + ", body: " + body);
                    Connection btCon = new BluetoothConnectionInsecure(mac);
                    btCon.open();
                    btCon.write(body.getBytes());
                    Thread.sleep(500);
                    btCon.close();
                    return "Test Printing Completed";
                } catch (Exception e) {
                    Timber.e(e.getMessage());
                    return "Test Printing Error with " + mac;
                }
            }
        });
    }

    public static Observable<String> printFullLabel (final String mac) {
        return Observable.fromCallable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                try {
                    Connection btCon = new BluetoothConnectionInsecure(mac);
                    btCon.open();
                    Pair<Integer, Integer> logo = storeImage(btCon, "/storage/sdcard0/ShipRight/companylogo.jpg", "logo");
                    Pair<Integer, Integer> signature = storeImage(btCon, "/storage/sdcard0/ShipRight/Test15.jpg", "signature");
                    String zplString = createZplBody(logo, signature);
                    btCon.write(zplString.getBytes());
                    Thread.sleep(500);
                    btCon.close();
                    return "Full Label Printing Completed";
                } catch (Exception e) {
                    Timber.e(e.getMessage());
                    return "Full Label Printing Error with " + mac;
                }
            }
        });
    }

    public static Pair<Integer, Integer> storeImage(Connection connection, String image, String name) {
        //Stores the specified image to the connected printer as a monochrome image. The image will be stored on the printer at
        //printerDriveAndFileName with the extension GRF. If a drive letter is not supplied, E will be used as the default (e.g. FILE
        // becomes E:FILE.GRF). If an extension is supplied, it is ignored (E:FILE.BMP becomes E:FILE.GRF). If the image resolution is
        // large (e.g. 1024x768) this method may take a long time to execute or throw an OutOfMemoryError exception.
        // printer.storeImage("E:" + fileName.toUpperCase(), zebraImage, width, height);
        Timber.d("This stores an image on the bluetooth printer and then prints it - WORKING!");
        try {
            File file = new File(image);
            Timber.d("The file is located at: " + file.getAbsolutePath() + " and is " + file.exists());
            Bitmap bitmap = BitmapFactory.decodeFile(image, null);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
            ZebraImageI zia = ZebraImageFactory.getImage(bitmap);
            ZebraPrinter printer = ZebraPrinterFactory.getInstance(PrinterLanguage.ZPL, connection);  // Added as an attempt for images
            printer.storeImage(name, zia, bitmap.getWidth(), bitmap.getHeight());
            Timber.d("store E:" + name + ".grf - " + calcCenter(bitmap.getWidth()) + ", 0, " + bitmap.getWidth() + ", " + bitmap
                    .getHeight());
            return Pair.create(bitmap.getWidth(), bitmap.getHeight());
        } catch (Exception e) {
            Timber.e("Error: " + e.getMessage());
            return null;
        }
    }

    public static void printImage(Connection connection, String image) {
        // printer.printImage(new ZebraImageAndroid(bitmap), x, y, width, height, false);
        // zia = the image to be printed.
        // x = horizontal starting position in dots, y = vertical starting position in dots.
        // width = desired width of the printed image. Passing a value less than 1 will preserve original width.
        // height = desired height of the printed image. Passing a value less than 1 will preserve original height.
        // insideFormat = boolean value indicating whether this image should be printed by itself (false), or is part of a format
        // being written to the connection (true).
        Timber.d("This prints an image to the bluetooth printer by streaming the image directly to the printer - WORKING!");
        try {
            File file = new File(image);
            Timber.d("The file is located at: " + file.getAbsolutePath() + " and is " + file.exists());
            Bitmap bitmap = BitmapFactory.decodeFile(image, null);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
            //ZebraImageAndroid zia = new ZebraImageAndroid(bitmap);
            ZebraImageI zia = ZebraImageFactory.getImage(bitmap);
            ZebraPrinter printer = ZebraPrinterFactory.getInstance(PrinterLanguage.ZPL, connection);  // Added as an attempt for images
            printer.printImage(zia, calcCenter(bitmap.getWidth()), 0, bitmap.getWidth(), bitmap.getHeight(), false);
            Timber.d("print " + image + " - " + calcCenter(bitmap.getWidth()) + ", 0, " + bitmap.getWidth() + ", " + bitmap.getHeight());
        } catch (Exception e) {
            Timber.e("Error: " + e.getMessage());
        }
    }

    private static String createZplBody(Pair<Integer, Integer> logo, Pair<Integer, Integer> signature) {
        String zpl = "";
        int y = 0;
        // Print out the header image
        zpl = zpl + "^FO" + calcCenter(logo.first) + ",0^XGE:logo.grf," + logo.first + "," + logo.second + "^FS";
        y = logo.second + 40;
        // Create Header for the Label
        zpl = zpl + "^FO35," + (y+8) + "^A0N,20,20";                   // FO = Field Origin, A = Font
        zpl = zpl + "^FD" + "Jay Kallen" + "^FS";           // Field Data, Field Separator
        zpl = zpl + "^FO265," + y + "^A0N,40,40";
        zpl = zpl + "^FD" + "Delivery Summary" + "^FS";
        zpl = zpl + "^FO600," + (y+8) + "^A0N,20,20";
        zpl = zpl + "^FD" + Helper.getCurrDateTimeWest() + "^FS";
        zpl = zpl + "^FO30," + (y+40) + "^GB780,0,4^FS";                //Graphic Box: Width in dots, height in dots, border thickness
        y+=50;
        // Body of the Label
        zpl = zpl + "^FO30," + y + "^A0N,35,35";
        zpl = zpl + "^FD" + "Invoice #: " + "117625" + "^FS";
        zpl = zpl + "^FO400," + y + "^A0N,35,35";
        zpl = zpl + "^FD" + "Invoice Amt: " + "$2,439.92" + "^FS";
        zpl = zpl + "^FO75," + (y+=40) + "^A0N,35,25";
        zpl = zpl + "^FD" + "TOTES" + "^FS";
        zpl = zpl + "^FO300," + y + "^A0N,35,25";
        zpl = zpl + "^FD" + "5" + "^FS";
        zpl = zpl + "^FO75," + (y+=40) + "^A0N,35,25";
        zpl = zpl + "^FD" + "BUNDLES" + "^FS";
        zpl = zpl + "^FO300," + y + "^A0N,35,25";
        zpl = zpl + "^FD" + "35" + "^FS";
        zpl = zpl + "^FO30," + (y+=30) + "^GB780,0,4^FS";
        zpl = zpl + "^FO30," + (y+=20) + "^A0N,35,35";
        zpl = zpl + "^FD" + "Invoice #: " + "117625" + "^FS";
        zpl = zpl + "^FO400," + y + "^A0N,35,35";
        zpl = zpl + "^FD" + "Invoice Amt: " + "$2,439.92" + "^FS";
        zpl = zpl + "^FO75," + (y+=40) + "^A0N,35,25";
        zpl = zpl + "^FD" + "TOTES" + "^FS";
        zpl = zpl + "^FO300," + y + "^A0N,35,25";
        zpl = zpl + "^FD" + "5" + "^FS";
        zpl = zpl + "^FO75," + (y+=40) + "^A0N,35,25";
        zpl = zpl + "^FD" + "BUNDLES" + "^FS";
        zpl = zpl + "^FO300," + y + "^A0N,35,25";
        zpl = zpl + "^FD" + "35" + "^FS";
        zpl = zpl + "^FO30," + (y+=40) + "^GB780,0,4^FS";
        zpl = zpl + "^FO30," + (y+=10) + "^A0N,35,35";
        zpl = zpl + "^FD" + "Totes Returned: 2" + "^FS";
        zpl = zpl + "^FO30," + (y+=40) + "^GB780,0,4^FS";
        // Print out the signature image
        zpl = zpl + "^FO" + calcCenter(signature.first) + "," + (y+=40) + "^XGE:signature.grf," + signature.first + "," +
                signature.second + "^FS";
        y = y + signature.second + 40;
        // Print out the footer
        zpl = zpl + "^FO" + calcCenter(400)  + "," + y + "^A0N,30,20";
        zpl = zpl + "^FD" + "Jose Alcaraz" + " Date/Time: " + Helper.getCurrDateTimeWest() + "^FS";
        zpl = zpl + "^FO40," + (y+=60) + "^A0N,30,20";
        zpl = zpl + "^FD" + "MERCHANDISE RECEIVED EXCEPT AS NOTED. ACCOUNT AGREES TO PAY ALL COSTS" + "^FS";
        zpl = zpl + "^FO40," + (y+=40) + "^A0N,30,20";
        zpl = zpl + "^FD" + "OF COLLECTING ANY BALANCE PAST DUE ON THIS INVOICE, INCLUDING ALL REASONABLE" + "^FS";
        zpl = zpl + "^FO40," + (y+=40) + "^A0N,30,20";
        zpl = zpl + "^FD" + "ATTORNEY FEES. A FINANCE CHARGE OF 1 1/2% PER MONTH WILL BE CHARGED ON" + "^FS";
        zpl = zpl + "^FO40," + (y+=40) + "^A0N,30,20";
        zpl = zpl + "^FD" + "ALL PAST DUE INVOICES. CUSTOMER HAS 24 HOURS TO CALL IN ANY DISCREPANCIES." + "^FS";
        zpl = zpl + "^XZ";
        // Add the beginning of the label: XA = Start Format, LL = Label Length, POI = Print Orientation Inverse
        zpl = "^XA^LL" + (y+=100) + "^POI" + zpl;
        Timber.d("Full length of the label is " + y);
        return zpl;
    }

    private static int calcCenter(int width) {
        return (width < 800) ? (800 - width) / 2 : 0;
    }

}
