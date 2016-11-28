/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ebus.internal.connection;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jssc.SerialPort;
import jssc.SerialPortException;

/**
 * This is the serial implementation of the eBus connector. It only handles
 * serial specific connection/disconnection. All logic is handled by
 * abstract class AbstractEBusConnector.
 *
 * @author Christian Sowada
 * @since 1.7.0
 */
public class EBusSerialConnector2 extends AbstractEBusWriteConnector {

    private static final Logger logger = LoggerFactory.getLogger(EBusSerialConnector2.class);

    /** The serial object */
    private SerialPort serialPort;

    /** The serial port name */
    private String port;

    /** output stream for eBus communication */
    // private OutputStream outputStream;

    /** input stream for eBus communication */
    // private InputStream inputStream;

    /**
     * @param port
     */
    public EBusSerialConnector2(String port) {
        logger.info("Use JSSC EBus connector !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        this.port = port;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.ebus.connection.AbstractEBusConnector#connect()
     */
    @Override
    protected boolean connect() throws IOException {

        serialPort = new SerialPort(port);
        try {
            serialPort.openPort();
            serialPort.setParams(2400, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            // serialPort.
        } catch (SerialPortException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return super.connect();

        // try {
        //
        // final CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(port);
        //
        // if (portIdentifier != null) {
        //
        // serialPort = portIdentifier.open("org.openhab.binding.ebus", 2000);
        // serialPort.setSerialPortParams(2400, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
        // SerialPort.PARITY_NONE);
        //
        // // set timeout 10 sec.
        // serialPort.disableReceiveThreshold();
        // serialPort.enableReceiveTimeout(10000);
        //
        // // use event to let readByte wait until data is available, optimize cpu usage
        // serialPort.addEventListener(this);
        // serialPort.notifyOnDataAvailable(true);
        //
        // outputStream = serialPort.getOutputStream();
        // inputStream = serialPort.getInputStream();
        //
        // return super.connect();
        // }
        //
        // } catch (NoSuchPortException e) {
        // logger.error("Unable to connect to serial port {}", port);
        //
        // } catch (PortInUseException e) {
        // logger.error("Serial port {} is already in use", port);
        //
        // } catch (UnsupportedCommOperationException e) {
        // logger.error(e.toString(), e);
        //
        // } catch (TooManyListenersException e) {
        // logger.error("Too many listeners error!", e);
        // }
        //
        // serialPort = null;
        // return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.ebus.connection.AbstractEBusConnector#disconnect()
     */
    @Override
    protected boolean disconnect() throws IOException {

        if (serialPort == null) {
            return true;
        }

        // run the serial.close in a new not-interrupted thread to
        // prevent an IllegalMonitorStateException error
        Thread shutdownThread = new Thread(new Runnable() {
            @Override
            public void run() {

                // IOUtils.closeQuietly(inputStream);
                // IOUtils.closeQuietly(outputStream);

                if (serialPort != null) {
                    try {
                        if (serialPort.isOpened()) {
                            serialPort.closePort();
                        }
                    } catch (SerialPortException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    serialPort = null;
                }
            }
        }, "eBUS serial shutdown thread");

        shutdownThread.start();

        try {
            // wait for shutdown
            shutdownThread.join(2000);
        } catch (InterruptedException e) {
        }

        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see gnu.io.SerialPortEventListener#serialEvent(gnu.io.SerialPortEvent)
     */
    // @Override
    // public void serialEvent(SerialPortEvent event) {
    // if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
    // synchronized (inputStream) {
    // inputStream.notifyAll();
    // }
    // }
    // }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.ebus.internal.connection.AbstractEBusConnector#readByte()
     */
    @Override
    protected int readByte(boolean lowLatency) throws IOException {
        try {
            int[] data = serialPort.readIntArray(1, 3000);
            return data[0];

        } catch (SerialPortException e) {
            e.printStackTrace();
        }

        return -1;

        // if (lowLatency) {
        // return inputStream.read();
        // } else {
        // if (inputStream.available() > 0) {
        // return inputStream.read();
        //
        // } else {
        // synchronized (inputStream) {
        // try {
        // inputStream.wait(3000);
        // return inputStream.read();
        // } catch (InterruptedException e) {
        // Thread.currentThread().interrupt();
        // }
        // return -1;
        // }
        // }
        // }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.ebus.internal.connection.AbstractEBusWriteConnector#writeByte(int)
     */
    @Override
    protected void writeByte(int b) throws IOException {
        try {
            serialPort.writeInt(b);
        } catch (SerialPortException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // no flush, sometimes it blocks infinitely
    }

    @Override
    protected boolean isReceiveBufferEmpty() throws IOException {
        // return inputStream.available() == 0;
        try {
            return serialPort.getInputBufferBytesCount() == 0;
        } catch (SerialPortException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return false;
    }

    @Override
    protected boolean isConnected() {
        return serialPort != null ? serialPort.isOpened() : false;
    }

    @Override
    protected void resetInputBuffer() throws IOException {
        try {
            serialPort.purgePort(SerialPort.PURGE_TXCLEAR);
        } catch (SerialPortException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // serialPort.
        // inputStream.skip(inputStream.available());
    }

    @Override
    protected InputStream getInputStream() {
        // serialPort.
        return null;
    }
}
