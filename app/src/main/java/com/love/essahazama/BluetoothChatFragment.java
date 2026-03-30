package com.love.essahazama;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BluetoothChatFragment extends Fragment {

    private TextView tvStatus;
    private EditText etMessage;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothChatHelper chatHelper;
    private List<String> chatMessages;
    private ChatAdapter chatAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bluetooth_chat, container, false);

        tvStatus = view.findViewById(R.id.tvStatus);
        etMessage = view.findViewById(R.id.etMessage);
        Button btnConnect = view.findViewById(R.id.btnConnect);
        ImageButton btnSend = view.findViewById(R.id.btnSend);
        RecyclerView rvChat = view.findViewById(R.id.rvChat);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages);
        rvChat.setLayoutManager(new LinearLayoutManager(getContext()));
        rvChat.setAdapter(chatAdapter);

        btnConnect.setOnClickListener(v -> showPairedDevicesDialog());

        btnSend.setOnClickListener(v -> {
            String msg = etMessage.getText().toString().trim();
            if (!msg.isEmpty() && chatHelper != null && chatHelper.getState() == BluetoothChatHelper.STATE_CONNECTED) {
                chatHelper.write(msg.getBytes());
                etMessage.setText("");
            } else {
                Toast.makeText(getContext(), "تأكد من الاتصال أولاً", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (chatHelper == null) chatHelper = new BluetoothChatHelper(getContext(), mHandler);
        chatHelper.start();
    }

    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatHelper.STATE_CONNECTED: tvStatus.setText("الحالة: متصل 💜"); break;
                        case BluetoothChatHelper.STATE_CONNECTING: tvStatus.setText("جاري الاتصال..."); break;
                        case BluetoothChatHelper.STATE_LISTEN:
                        case BluetoothChatHelper.STATE_NONE: tvStatus.setText("الحالة: غير متصل"); break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    chatMessages.add("أنا: " + new String(writeBuf));
                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMsg = new String(readBuf, 0, msg.arg1);
                    chatMessages.add("حبيبي: " + readMsg);
                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                    break;
            }
        }
    };

    @SuppressLint("MissingPermission")
    private void showPairedDevicesDialog() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        List<String> deviceNames = new ArrayList<>();
        List<BluetoothDevice> devices = new ArrayList<>();

        for (BluetoothDevice device : pairedDevices) {
            deviceNames.add(device.getName() + "\n" + device.getAddress());
            devices.add(device);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("اختر جهاز عيسى/حزامه");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, deviceNames);
        builder.setAdapter(adapter, (dialog, which) -> {
            chatHelper.connect(devices.get(which));
        });
        builder.show();
    }

    private static class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
        private final List<String> messages;
        ChatAdapter(List<String> messages) { this.messages = messages; }
        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int t) {
            TextView tv = new TextView(p.getContext());
            tv.setPadding(16, 8, 16, 8);
            tv.setTextColor(0xFFFFFFFF);
            return new ViewHolder(tv);
        }
        @Override public void onBindViewHolder(@NonNull ViewHolder h, int p) { ((TextView) h.itemView).setText(messages.get(p)); }
        @Override public int getItemCount() { return messages.size(); }
        static class ViewHolder extends RecyclerView.ViewHolder { ViewHolder(View v) { super(v); } }
    }
}
