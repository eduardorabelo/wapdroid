/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/bryan/development/wapdroid/src/com/piusvelte/wapdroid/IWapdroidService.aidl
 */
package com.piusvelte.wapdroid;
public interface IWapdroidService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.piusvelte.wapdroid.IWapdroidService
{
private static final java.lang.String DESCRIPTOR = "com.piusvelte.wapdroid.IWapdroidService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.piusvelte.wapdroid.IWapdroidService interface,
 * generating a proxy if needed.
 */
public static com.piusvelte.wapdroid.IWapdroidService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.piusvelte.wapdroid.IWapdroidService))) {
return ((com.piusvelte.wapdroid.IWapdroidService)iin);
}
return new com.piusvelte.wapdroid.IWapdroidService.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_setCallback:
{
data.enforceInterface(DESCRIPTOR);
android.os.IBinder _arg0;
_arg0 = data.readStrongBinder();
this.setCallback(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_updatePreferences:
{
data.enforceInterface(DESCRIPTOR);
boolean _arg0;
_arg0 = (0!=data.readInt());
int _arg1;
_arg1 = data.readInt();
boolean _arg2;
_arg2 = (0!=data.readInt());
boolean _arg3;
_arg3 = (0!=data.readInt());
boolean _arg4;
_arg4 = (0!=data.readInt());
boolean _arg5;
_arg5 = (0!=data.readInt());
boolean _arg6;
_arg6 = (0!=data.readInt());
int _arg7;
_arg7 = data.readInt();
this.updatePreferences(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5, _arg6, _arg7);
reply.writeNoException();
return true;
}
case TRANSACTION_suspendWifiControl:
{
data.enforceInterface(DESCRIPTOR);
this.suspendWifiControl();
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.piusvelte.wapdroid.IWapdroidService
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
public void setCallback(android.os.IBinder mWapdroidUIBinder) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder(mWapdroidUIBinder);
mRemote.transact(Stub.TRANSACTION_setCallback, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void updatePreferences(boolean manage, int interval, boolean notify, boolean vibrate, boolean led, boolean ringtone, boolean batteryOverride, int batteryPercentage) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(((manage)?(1):(0)));
_data.writeInt(interval);
_data.writeInt(((notify)?(1):(0)));
_data.writeInt(((vibrate)?(1):(0)));
_data.writeInt(((led)?(1):(0)));
_data.writeInt(((ringtone)?(1):(0)));
_data.writeInt(((batteryOverride)?(1):(0)));
_data.writeInt(batteryPercentage);
mRemote.transact(Stub.TRANSACTION_updatePreferences, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void suspendWifiControl() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_suspendWifiControl, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_setCallback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_updatePreferences = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_suspendWifiControl = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
}
public void setCallback(android.os.IBinder mWapdroidUIBinder) throws android.os.RemoteException;
public void updatePreferences(boolean manage, int interval, boolean notify, boolean vibrate, boolean led, boolean ringtone, boolean batteryOverride, int batteryPercentage) throws android.os.RemoteException;
public void suspendWifiControl() throws android.os.RemoteException;
}
