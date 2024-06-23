package cc.ioctl.tmoe.hook.func;


import android.text.TextPaint;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import cc.ioctl.tmoe.base.annotation.FunctionHookEntry;
import cc.ioctl.tmoe.hook.base.CommonDynamicHook;
import cc.ioctl.tmoe.util.HookUtils;
import cc.ioctl.tmoe.util.Initiator;

@FunctionHookEntry
public class ShowMsgId extends CommonDynamicHook {

    public static final ShowMsgId INSTANCE = new ShowMsgId();

    private ShowMsgId() {
    }

    @Override
    public boolean initOnce() throws Exception {
        Class<?> kChatMessageCell = Initiator.loadClass("org.telegram.ui.Cells.ChatMessageCell");
        Method measureTime = kChatMessageCell.getDeclaredMethod("measureTime",
                Initiator.loadClass("org.telegram.messenger.MessageObject"));
        Field currentTimeString = kChatMessageCell.getDeclaredField("currentTimeString");
        currentTimeString.setAccessible(true);
        Field messageOwner = Initiator.loadClass("org.telegram.messenger.MessageObject").getDeclaredField("messageOwner");
        messageOwner.setAccessible(true);
        Field msgId = Initiator.loadClass("org.telegram.tgnet.TLRPC$Message").getDeclaredField("id");
        msgId.setAccessible(true);
        Field msgDate = Initiator.loadClass("org.telegram.tgnet.TLRPC$Message").getDeclaredField("date");
        msgDate.setAccessible(true);
        Class<?> kTheme = Initiator.loadClass("org.telegram.ui.ActionBar.Theme");
        Field chatTimePaint = kTheme.getDeclaredField("chat_timePaint");
        chatTimePaint.setAccessible(true);
        Field timeTextWidth = kChatMessageCell.getDeclaredField("timeTextWidth");
        timeTextWidth.setAccessible(true);
        Field timeWidth = kChatMessageCell.getDeclaredField("timeWidth");
        timeWidth.setAccessible(true);
        Field dateKey = Initiator.loadClass("org.telegram.messenger.MessageObject").getDeclaredField("dateKeyInt");
        dateKey.setAccessible(true);
        HookUtils.hookAfterIfEnabled(this, measureTime, param -> {

            CharSequence time = (CharSequence) currentTimeString.get(param.thisObject);
            assert time != null;
            Object messageObject = param.args[0];
            Object owner = messageOwner.get(messageObject);
//            int id = msgId.getInt(owner);
//            String delta = id + " ";
//            time = delta + time;
            int date = msgDate.getInt(owner);
            String formattedTime = null;

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                LocalDateTime dateTime = LocalDateTime.ofEpochSecond(date, 0, ZoneOffset.ofHours(3));
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                formattedTime = dateTime.format(formatter);
            }
            currentTimeString.set(param.thisObject, formattedTime);

            assert formattedTime != null;
            TextPaint paint = (TextPaint) chatTimePaint.get(null);
            int lenght = (int) Math.ceil(paint.measureText(formattedTime));

            timeTextWidth.setInt(param.thisObject, lenght);
            timeWidth.setInt(param.thisObject, lenght);
//            TextPaint paint = (TextPaint) chatTimePaint.get(null);
//            assert paint != null;
//            int deltaWidth = (int) Math.ceil(paint.measureText(delta));
//            timeTextWidth.setInt(param.thisObject, deltaWidth + timeTextWidth.getInt(param.thisObject));
//            timeWidth.setInt(param.thisObject, deltaWidth + timeWidth.getInt(param.thisObject));
//
        });
        return true;
    }
}
