package exo.mbariah.sharemylocation.Chat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import exo.mbariah.sharemylocation.R;


public class ChatMessageAdapter extends ArrayAdapter<ChatMessage> {

    private String lon, lat = null;

    private static final int MY_MESSAGE = 0, OTHER_MESSAGE = 1, MY_IMAGE = 2, OTHER_IMAGE = 3;

    public ChatMessageAdapter(Context context, List<ChatMessage> data) {
        super(context, R.layout.item_mine_message, data);
    }

    @Override
    public int getViewTypeCount() {
        // my message, other message, my image, other image
        return 4;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage item = getItem(position);

        if (item.isMine() && !item.isImage()) return MY_MESSAGE;
        else if (!item.isMine() && !item.isImage()) return OTHER_MESSAGE;
        else if (item.isMine() && item.isImage()) return MY_IMAGE;
        else return OTHER_IMAGE;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int viewType = getItemViewType(position);
        if (viewType == MY_MESSAGE) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_mine_message, parent, false);

            final String Link = getItem(position).getLink();
            final ImageView image = (ImageView) convertView.findViewById(R.id.url);
            //link.setText(getItem(position).getLink());
            image.setClickable(true);

            // regex for longitude and latitude
            String pattern = "(-?[\\d]*\\.[\\d]*),(-?[\\d]*\\.[\\d]*)";

            // Create a Pattern object
            Pattern r = Pattern.compile(pattern);

            // Now create matcher object.
            Matcher m = r.matcher(Link);
            if (m.find()) {
                lon = m.group(1);
                lat = m.group(2);
            } else {
                System.out.println("NO MATCH");
            }
            final String maps ="https://maps.google.com/maps?q=loc:" + lon + "," + lat + "(Here)";


            try {

                Picasso.with(getContext())
                        .load(Link)
                        .placeholder(R.drawable.broken)
                        .error(R.drawable.broken)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .fit()
                        .centerCrop()
                        .into(image, new Callback() {
                            @Override
                            public void onSuccess() {
                                //successfully loads from CACHE
                                image.setClickable(true);

                                image.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        Uri uri = Uri.parse(maps);
                                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
                                        getContext().startActivity(intent);

                                    }
                                });

                            }

                            @Override
                            public void onError() {
                                // fetch online
                                Picasso.with(getContext())
                                        .load(Link)
                                        .fetch(new Callback() {
                                            @Override
                                            public void onSuccess() {
                                                Picasso.with(getContext())
                                                        .load(Link)
                                                        .fit()
                                                        .centerCrop()
                                                        .into(image, new com.squareup.picasso.Callback() {
                                                            @Override
                                                            public void onSuccess() {
                                                                image.setClickable(true);

                                                                image.setOnClickListener(new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View v) {

                                                                        Uri uri = Uri.parse(maps);
                                                                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
                                                                        getContext().startActivity(intent);

                                                                    }
                                                                });

                                                            }

                                                            @Override
                                                            public void onError() {
                                                                //Toast.makeText(getContext(), "No Image Found. Try again later", Toast.LENGTH_SHORT).show();
                                                                image.setClickable(false);
                                                                //pv_circular.stop();
                                                            }
                                                        });
                                            }

                                            @Override
                                            public void onError() {
                                                //NO IMAGE offline or online
                                                //Toast.makeText(getContext(), "No Image Found. Try again later", Toast.LENGTH_SHORT).show();
                                                image.setClickable(false);
                                                //pv_circular.stop();
                                            }
                                        });
                            }
                        });


            } catch (Exception e) {
            }

            if (getItem(position).getLink().equals("empty")) {
                image.setVisibility(View.GONE);
            }

            TextView textView = (TextView) convertView.findViewById(R.id.text);
            textView.setText(getItem(position).getContent());

            if (getItem(position).getContent().isEmpty()) {
                textView.setVisibility(View.GONE);
            }

            TextView date = (TextView) convertView.findViewById(R.id.date);
            date.setText(getItem(position).getDate());

        } else if (viewType == OTHER_MESSAGE) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_other_message, parent, false);

            final String Link = getItem(position).getLink();
            final ImageView image = (ImageView) convertView.findViewById(R.id.url);
            //link.setText(getItem(position).getLink());
            image.setClickable(true);

            // regex for longitude and latitude
            String pattern = "(-?[\\d]*\\.[\\d]*),(-?[\\d]*\\.[\\d]*)";
            Pattern r = Pattern.compile(pattern);

            // Now create matcher object.
            Matcher m = r.matcher(Link);
            if (m.find()) {
                lon = m.group(1);
                lat = m.group(2);
            } else {
                System.out.println("NO MATCH");
            }
            final String maps ="https://maps.google.com/maps?q=loc:" + lon + "," + lat + "%20(Here)";

            try {

                Picasso.with(getContext())
                        .load(Link)
                        .placeholder(R.drawable.broken)
                        .error(R.drawable.broken)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .fit()
                        .centerCrop()
                        .into(image, new Callback() {
                            @Override
                            public void onSuccess() {
                                //successfully loads from CACHE
                                image.setClickable(true);
                                ////v_circular.stop();
                                image.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        Uri uri = Uri.parse(maps);
                                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
                                        getContext().startActivity(intent);

                                    }
                                });
                            }

                            @Override
                            public void onError() {
                                // fetch online
                                Picasso.with(getContext())
                                        .load(Link)
                                        .fetch(new Callback() {
                                            @Override
                                            public void onSuccess() {
                                                Picasso.with(getContext())
                                                        .load(Link)
                                                        .fit()
                                                        .centerCrop()
                                                        .into(image, new com.squareup.picasso.Callback() {
                                                            @Override
                                                            public void onSuccess() {
                                                                image.setClickable(true);

                                                                image.setOnClickListener(new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View v) {

                                                                        Uri uri = Uri.parse(maps);
                                                                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
                                                                        getContext().startActivity(intent);

                                                                    }
                                                                });

                                                            }

                                                            @Override
                                                            public void onError() {
                                                                //Toast.makeText(getContext(), "No Image Found. Try again later", Toast.LENGTH_SHORT).show();
                                                                image.setClickable(false);
                                                                //pv_circular.stop();
                                                            }
                                                        });
                                            }

                                            @Override
                                            public void onError() {
                                                //NO IMAGE offline or online
                                                //Toast.makeText(getContext(), "No Image Found. Try again later", Toast.LENGTH_SHORT).show();
                                                image.setClickable(false);
                                                //pv_circular.stop();
                                            }
                                        });
                            }
                        });


            } catch (Exception e) {
            }

            if (getItem(position).getLink().equals("empty")) {
                image.setVisibility(View.GONE);
            }

            TextView textView = (TextView) convertView.findViewById(R.id.text);
            textView.setText(getItem(position).getContent());

            if (getItem(position).getContent().isEmpty()) {
                textView.setVisibility(View.GONE);
            }

            TextView date = (TextView) convertView.findViewById(R.id.date);
            date.setText(getItem(position).getDate());

        } else if (viewType == MY_IMAGE) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_mine_image, parent, false);
        } else {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_other_image, parent, false);
        }

        convertView.findViewById(R.id.chatMessageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getContext(), "onClick", Toast.LENGTH_LONG).show();
            }
        });


        return convertView;
    }
}
