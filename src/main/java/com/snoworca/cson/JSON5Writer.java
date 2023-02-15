package com.snoworca.cson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;

public class JSON5Writer {

    private final static int DEFAULT_BUFFER_SIZE = 4096;


    private ArrayDeque<ObjectType> mTypeStack = new ArrayDeque<>();
    private ByteArrayOutputStream mBufferStream = new ByteArrayOutputStream(DEFAULT_BUFFER_SIZE);



    public JSON5Writer() {
        mTypeStack.addLast(ObjectType.None);
        try {
            mBufferStream.write(CSONDataType.PREFIX);
            mBufferStream.write(CSONDataType.VER_RAW);
        } catch (IOException ignored) {}
    }




    private void writeString(byte[] buffer) {
        // 총 16 개 버퍼 저장 가능. 1바이트 사용.
        if(buffer.length < 16) {

            byte typeAndLen = (byte)(CSONDataType.TYPE_STRING_SHORT | buffer.length);
            mBufferStream.write(typeAndLen);
            if(buffer.length == 0) {
                return;
            }
        }
        else if(buffer.length < 4095) {
            // 총 4094 개 버퍼 저장 가능. 2바이트 사용.
            byte typeAndLen = (byte)(CSONDataType.TYPE_STRING_MIDDLE | ((buffer.length & 0x00000F00) >> 8));
            byte lenNext = (byte)(buffer.length & 0x000000FF);

            mBufferStream.write(typeAndLen);
            mBufferStream.write(lenNext);

            // 되돌리기
            //int lenFirst = ((int)(typeAndLen & 0x0F) << 8);
            //int size = (lenFirst | (int)(lenNext & 0xFF));
        } else {
            // 총 1048574 개의 버퍼 저장 가능. 총 3바이트 사용.
            byte typeAndLen = (byte)(CSONDataType.TYPE_STRING_LONG | ((buffer.length & 0x000F0000) >> 16));
            byte lenNextA = (byte)((buffer.length & 0x0000FF00) >> 8);
            byte lenNextB = (byte)(buffer.length & 0x000000FF);

            // 되돌리기
            //int lenFirst = ((int)(typeAndLen & 0x0F) << 16);
            //int size =  lenFirst | ((int)(lenNextA & 0xff) << 8) | (int)(lenNextB & 0xff);
            mBufferStream.write(typeAndLen);
            mBufferStream.write(lenNextA);
            mBufferStream.write(lenNextB);
        }
        mBufferStream.write(buffer, 0 ,buffer.length );
    }

    private void writeBuffer(byte[] buffer) {
        // 총 16 개 버퍼 저장 가능. 1바이트 사용.
        if(buffer.length < 4095) {
            // 총 4094 개 버퍼 저장 가능. 2바이트 사용.
            byte typeAndLen = (byte)(CSONDataType.TYPE_RAW_MIDDLE | ((buffer.length & 0x00000F00) >> 8));
            byte lenNext = (byte)(buffer.length & 0x000000FF);

            mBufferStream.write(typeAndLen);
            mBufferStream.write(lenNext);

            if(buffer.length == 0) {
                return;
            }

            // 되돌리기
            //int lenFirst = ((int)(typeAndLen & 0x0F) << 8);
            //int size = (lenFirst | (int)(lenNext & 0xFF));
        } else if(buffer.length < 1048574) {
            // 총 1048574 개의 버퍼 저장 가능. 총 3바이트 사용.
            byte typeAndLen = (byte)(CSONDataType.TYPE_RAW_LONG | ((buffer.length & 0x000F0000) >> 16));
            byte lenNextA = (byte)((buffer.length & 0x0000FF00) >> 8);
            byte lenNextB = (byte)(buffer.length & 0x000000FF);

            // 되돌리기
            //int lenFirst = ((int)(typeAndLen & 0x0F) << 16);
            //int size =  lenFirst | ((int)(lenNextA & 0xff) << 8) | (int)(lenNextB & 0xff);
            mBufferStream.write(typeAndLen);
            mBufferStream.write(lenNextA);
            mBufferStream.write(lenNextB);
        }
        else  {
            // 총 4G 저장 가능 총. 5바이트 사용.
            byte type = CSONDataType.TYPE_RAW_WILD;
            writeInt(buffer.length);
            mBufferStream.write(type);
        }
        mBufferStream.write(buffer, 0 ,buffer.length );
    }



    private void writeFloat(float data) {
        int floatValue = Float.floatToIntBits(data);
        writeInt(floatValue);
    }

    private void writeDouble(double data) {
        long doubleValue = Double.doubleToLongBits(data);
        writeLong(doubleValue);
    }


    private void writeShort(short value) {
        mBufferStream.write((byte) (value >> 8));
        mBufferStream.write((byte) (value));
    }

    private void writeInt(int value) {
        mBufferStream.write((byte) (value >> 24));
        mBufferStream.write((byte) (value >> 16));
        mBufferStream.write((byte) (value >> 8));
        mBufferStream.write((byte) (value));
    }

    private void writeLong(long value) {
        mBufferStream.write((byte) (value >> 56));
        mBufferStream.write((byte) (value >> 48));
        mBufferStream.write((byte) (value >> 40));
        mBufferStream.write((byte) (value >> 32));
        mBufferStream.write((byte) (value >> 24));
        mBufferStream.write((byte) (value >> 16));
        mBufferStream.write((byte) (value >> 8));
        mBufferStream.write((byte) (value));

    }

    public JSON5Writer key(char key) {
        if(mTypeStack.getLast() != ObjectType.Object) {
            throw new CSONWriteException();
        }
        mTypeStack.addLast(ObjectType.ObjectKey);
        writeString((key + "").getBytes(StandardCharsets.UTF_8));
        return this;
    }

    public JSON5Writer key(String key) {
        if(mTypeStack.getLast() != ObjectType.Object) {
            throw new CSONWriteException();
        }
        mTypeStack.addLast(ObjectType.ObjectKey);
        writeString(key.getBytes(StandardCharsets.UTF_8));
        return this;
    }

    public JSON5Writer nullValue() {
        if(mTypeStack.getLast() != ObjectType.ObjectKey) {
            throw new CSONWriteException();
        }
        mTypeStack.removeLast();
        mBufferStream.write(CSONDataType.TYPE_NULL);
        return this;
    }

    public JSON5Writer value(String value) {
        if(value== null) {
            nullValue();
            return this;
        }

        if(mTypeStack.getLast() != ObjectType.ObjectKey) {
            throw new CSONWriteException();
        }
        mTypeStack.removeLast();
        writeString(value.getBytes(StandardCharsets.UTF_8));
        return this;
    }

    public JSON5Writer value(byte[] value) {
        if(value== null) {
            nullValue();
            return this;
        }
        if(mTypeStack.getLast() != ObjectType.ObjectKey) {
            throw new CSONWriteException();
        }
        mTypeStack.removeLast();
        writeBuffer(value);
        return this;
    }

    public JSON5Writer value(BigDecimal value) {
        if(value== null) {
            nullValue();
            return this;
        }
        if(mTypeStack.getLast() != ObjectType.ObjectKey) {
            throw new CSONWriteException();
        }
        mTypeStack.removeLast();
        mBufferStream.write(CSONDataType.TYPE_BIGDECIMAL);
        writeString(value.toString().getBytes(StandardCharsets.UTF_8));
        return this;
    }

    public JSON5Writer value(byte value) {
        if(mTypeStack.getLast() != ObjectType.ObjectKey) {
            throw new CSONWriteException();
        }
        mTypeStack.removeLast();
        mBufferStream.write(CSONDataType.TYPE_BYTE);
        mBufferStream.write(value);
        return this;
    }

    public JSON5Writer value(int value) {
        if(mTypeStack.getLast() != ObjectType.ObjectKey) {
            throw new CSONWriteException();
        }

        mTypeStack.removeLast();
        mBufferStream.write(CSONDataType.TYPE_INT);
        writeInt(value);
        return this;
    }

    public JSON5Writer value(long value) {
        if(mTypeStack.getLast() != ObjectType.ObjectKey) {
            throw new CSONWriteException();
        }
        mTypeStack.removeLast();
        mBufferStream.write(CSONDataType.TYPE_LONG);
        writeLong(value);
        return this;
    }

    public JSON5Writer value(short value) {
        if(mTypeStack.getLast() != ObjectType.ObjectKey) {
            throw new CSONWriteException();
        }
        mTypeStack.removeLast();
        mBufferStream.write(CSONDataType.TYPE_SHORT);
        writeShort(value);
        return this;
    }

    public JSON5Writer value(boolean value) {
        if(mTypeStack.getLast() != ObjectType.ObjectKey) {
            throw new CSONWriteException();
        }
        mTypeStack.removeLast();
        mBufferStream.write(CSONDataType.TYPE_BOOLEAN);
        mBufferStream.write(value == true ? 1 : 0);
        return this;
    }

    public JSON5Writer value(char value) {
        if(mTypeStack.getLast() != ObjectType.ObjectKey) {
            throw new CSONWriteException();
        }
        mTypeStack.removeLast();
        mBufferStream.write(CSONDataType.TYPE_CHAR);
        writeShort((short)value);
        return this;
    }

    public JSON5Writer value(float value) {
        if(mTypeStack.getLast() != ObjectType.ObjectKey) {
            throw new CSONWriteException();
        }
        mTypeStack.removeLast();
        mBufferStream.write(CSONDataType.TYPE_FLOAT);
        writeFloat(value);
        return this;
    }

    public JSON5Writer value(double value) {
        if(mTypeStack.getLast() != ObjectType.ObjectKey) {
            throw new CSONWriteException();
        }
        mTypeStack.removeLast();
        mBufferStream.write(CSONDataType.TYPE_DOUBLE);
        writeDouble(value);
        return this;
    }

    ///
    public JSON5Writer addNull() {
        if(mTypeStack.getLast() != ObjectType.Array) {
            throw new CSONWriteException();
        }
        mBufferStream.write(CSONDataType.TYPE_NULL);
        return this;
    }

    public JSON5Writer add(String value) {
        if(value== null) {
            addNull();
            return this;
        }
        if(mTypeStack.getLast() != ObjectType.Array) {
            throw new CSONWriteException();
        }
        writeString(value.getBytes(StandardCharsets.UTF_8));
        return this;
    }

    public JSON5Writer add(byte[] value) {
        if(value== null) {
            addNull();
            return this;
        }
        if(mTypeStack.getLast() != ObjectType.Array) {
            throw new CSONWriteException();
        }
        writeBuffer(value);
        return this;
    }

    public JSON5Writer add(BigDecimal value) {
        if(value== null) {
            addNull();
            return this;
        }
        if(mTypeStack.getLast() != ObjectType.Array) {
            throw new CSONWriteException();
        }
        mBufferStream.write(CSONDataType.TYPE_BIGDECIMAL);
        writeString(value.toString().getBytes(StandardCharsets.UTF_8));
        return this;
    }


    public JSON5Writer add(byte value) {
        if(mTypeStack.getLast() != ObjectType.Array) {
            throw new CSONWriteException();
        }
        mBufferStream.write(CSONDataType.TYPE_BYTE);
        mBufferStream.write(value);
        return this;
    }

    public JSON5Writer add(int value) {
        if(mTypeStack.getLast() != ObjectType.Array) {
            throw new CSONWriteException();
        }
        mBufferStream.write(CSONDataType.TYPE_INT);
        writeInt(value);
        return this;
    }

    public JSON5Writer add(long value) {
        if(mTypeStack.getLast() != ObjectType.Array) {
            throw new CSONWriteException();
        }
        mBufferStream.write(CSONDataType.TYPE_LONG);
        writeLong(value);
        return this;
    }

    public JSON5Writer add(short value) {
        if(mTypeStack.getLast() != ObjectType.Array) {
            throw new CSONWriteException();
        }
        mBufferStream.write(CSONDataType.TYPE_SHORT);
        writeShort(value);
        return this;
    }

    public JSON5Writer add(boolean value) {
        if(mTypeStack.getLast() != ObjectType.Array) {
            throw new CSONWriteException();
        }
        mBufferStream.write(CSONDataType.TYPE_BOOLEAN);
        mBufferStream.write(value ? 1 : 0);
        return this;
    }

    public JSON5Writer add(JSON5Writer writer) {
        if(mTypeStack.getLast() != ObjectType.Array && writer.mTypeStack.getLast() != ObjectType.None) {
            throw new CSONWriteException();
        }
        byte[] buffer = writer.toByteArray();
        int headerSize = 1/*prefix size*/ + CSONDataType.VER_RAW.length;
        mBufferStream.write(buffer, headerSize, buffer.length - headerSize);
        return this;
    }

    public JSON5Writer add(char value) {
        if(mTypeStack.getLast() != ObjectType.Array) {
            throw new CSONWriteException();
        }
        mBufferStream.write(CSONDataType.TYPE_CHAR);
        writeShort((short)value);
        return this;
    }

    public JSON5Writer add(float value) {
        if(mTypeStack.getLast() != ObjectType.Array) {
            throw new CSONWriteException();
        }
        mBufferStream.write(CSONDataType.TYPE_FLOAT);
        writeFloat(value);
        return this;
    }

    public JSON5Writer add(double value) {
        if(mTypeStack.getLast() != ObjectType.Array) {
            throw new CSONWriteException();
        }
        mBufferStream.write(CSONDataType.TYPE_DOUBLE);
        writeDouble(value);
        return this;
    }



    public JSON5Writer openArray() {
        if(mTypeStack.getLast() != ObjectType.ObjectKey && mTypeStack.getLast() != ObjectType.Array && mTypeStack.getLast() != ObjectType.None) {
            throw new CSONWriteException();
        }
        mTypeStack.addLast(ObjectType.Array);
        mBufferStream.write(CSONDataType.TYPE_OPEN_ARRAY);
        return this;
    }

    public JSON5Writer closeArray() {
        if(mTypeStack.getLast() != ObjectType.Array) {
            throw new CSONWriteException();
        }

        mTypeStack.removeLast();
        if(mTypeStack.getLast() == ObjectType.ObjectKey) {
            mTypeStack.removeLast();
        }
        mBufferStream.write(CSONDataType.TYPE_CLOSE_ARRAY);
        return this;
    }

    public JSON5Writer openObject() {
        if(mTypeStack.getLast() == ObjectType.Object) {
            throw new CSONWriteException();
        }
        mTypeStack.addLast(ObjectType.Object);
        mBufferStream.write(CSONDataType.TYPE_OPEN_OBJECT);
        return this;
    }

    public JSON5Writer closeObject() {
        if(mTypeStack.getLast() != ObjectType.Object) {
            throw new CSONWriteException();
        }
        mTypeStack.removeLast();
        if(mTypeStack.getLast() == ObjectType.ObjectKey) {
            mTypeStack.removeLast();
        }
        mBufferStream.write(CSONDataType.TYPE_CLOSE_OBJECT);
        return this;
    }

    public byte[] toByteArray() {
        if(mTypeStack.getLast() != ObjectType.None) {
            throw new CSONWriteException();
        }
        return mBufferStream.toByteArray();
    }

}
