package kr.selfcontrol.selfwebfilter.internet;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.provider.Browser;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.net.URISyntaxException;

import kr.selfcontrol.selfwebfilter.R;
import kr.selfcontrol.selfwebfilter.service.FavoriteService;
import kr.selfcontrol.selfwebfilter.service.InternetService;
import kr.selfcontrol.selfwebfilter.view.MyWebView;

/**
 * Created by owner on 2015-12-18.
 */
public class InternetFragment extends Fragment implements TextView.OnEditorActionListener {

    private EditText urlText;
    private ImageButton drawerButton;
    private ImageButton button;
    private ProgressBar progressBar;
    public MyWebView webView;
    public String userAgent;

    private boolean shouldIShowBaiscUrl = false;
    private String basicUrl = "http://google.com";

    private View view;
    private OnUrlChanged mOnUrlChanged;
    private OnDrawerButtonListener mOnDrawerButtonListener;
    String baseDir = "";
    /////////////////////////
    private FrameLayout mTargetView;
    private FrameLayout mContentView;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;
    public View mCustomView;
    public MyWebChromeClient mClient;
    private InternetService internetService;

    //////////////////////////
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState == null) {
            //   webView.loadUrl("about:blank");
        } else {
            webView.restoreState(savedInstanceState);
        }
    }

    public void setBasicUrl(String url) {
        this.basicUrl = url;
        shouldIShowBaiscUrl = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        getInternetService().updateBlockData();
    }

    private InternetService getInternetService() {
        return InternetService.getInstance(getActivity().getApplicationContext());
    }

    private FavoriteService getFavoriteService() {
        return FavoriteService.getInstance(getActivity().getApplicationContext());
    }

    public interface OnDrawerButtonListener {
        void onDrawerButtonListener();
    }

    public void setOnDrawerButtonListener(OnDrawerButtonListener obj) {
        mOnDrawerButtonListener = obj;
    }

    public void onUrlChangedListener(OnUrlChanged obj) {
        mOnUrlChanged = obj;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getInternetService().updateBlockData();
        view = inflater.inflate(R.layout.content_internet, null);

        urlText = (EditText) view.findViewById(R.id.urlText);
        button = (ImageButton) view.findViewById(R.id.urlMoveButton);
        webView = (MyWebView) view.findViewById(R.id.webView);
        drawerButton = (ImageButton) view.findViewById(R.id.drawer_button);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        drawerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnDrawerButtonListener != null) {
                    mOnDrawerButtonListener.onDrawerButtonListener();
                }
            }

        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (urlText.getText().toString().equals("show")) {
                    showDeveloperMode();
                } else if (urlText.getText().toString().equals("source")) {
                    showSource();
                } else {
                    goUrl(urlText.getText().toString());
                }
            }
        });
        if (Build.VERSION.SDK_INT >= 19) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        mClient = new MyWebChromeClient();
        mContentView = (FrameLayout) view.findViewById(R.id.main_content);
        mTargetView = (FrameLayout) view.findViewById(R.id.target_view);
        webView.setWebChromeClient(mClient);
        webView.setWebViewClient(new MyWebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        if (shouldIShowBaiscUrl) {
            webView.loadUrl(basicUrl);
        } else {
            webView.loadData(getFavoriteService().getFavoriteUrlsInHtml(), "text/html", "UTF-8");
        }
        //webView.getSettings().setBlockNetworkImage(true);
        webView.getSettings().setDisplayZoomControls(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setSupportMultipleWindows(true);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.getSettings().setAppCacheEnabled(false);
        userAgent = webView.getSettings().getUserAgentString();
        webView.clearCache(true);
        webView.addJavascriptInterface(new LoadListener(), "HTMLOUT");

        webView.setDownloadListener(new DownloadListener() {

            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype,
                                        long contentLength) {
                DownloadManager.Request request = new DownloadManager.Request(
                        Uri.parse(url));

                final String[] separated = url.split("/");
                final String myFile = separated[separated.length - 1];
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); //Notify client once download is completed!
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, myFile);
                DownloadManager dm = (DownloadManager) getActivity().getSystemService(getActivity().DOWNLOAD_SERVICE);
                dm.enqueue(request);
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT); //This is important!
                intent.addCategory(Intent.CATEGORY_OPENABLE); //CATEGORY.OPENABLE
                intent.setType("*/*");//any application,any extension
                Toast.makeText(getActivity().getApplicationContext(), "Downloading File", //To notify the Client that the file is being downloaded
                        Toast.LENGTH_LONG).show();

            }
        });

        urlText.setOnEditorActionListener(this);

        return view;
    }

    public String getTitle() {
        if (webView != null && webView.getTitle() != null)
            return webView.getTitle().toString();
        return null;
    }

    private void goUrl(String url) {
        String full = url;
        String location = "";
        if (full.contains("://")) {
            location = full;
        } else {
            location = "http://" + full;
        }
        webView.loadUrl(location);
        setUrlText(location);
        setBasicUrl(location);
    }

    private void setUrlText(String url) {
        if (!urlText.isFocused()) {
            urlText.setText(url);
        }
    }


    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if ((actionId == EditorInfo.IME_ACTION_DONE) || (actionId == EditorInfo.IME_ACTION_NEXT) || event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
            goUrl(urlText.getText().toString());
        }
        return false;
    }

    public interface OnUrlChanged {
        void onUrlChanged(String url);
    }

    public class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            progressBar.setProgress(newProgress);
        }

        @Override
        public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, Message resultMsg) {
            return true;
        }

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {

            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            mCustomViewCallback = callback;
            mTargetView.addView(view);
            mCustomView = view;
            mContentView.setVisibility(View.GONE);
            mTargetView.setVisibility(View.VISIBLE);
            mTargetView.bringToFront();
        }

        @Override
        public void onHideCustomView() {
            if (mCustomView == null)
                return;

            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            mCustomView.setVisibility(View.GONE);
            mTargetView.removeView(mCustomView);
            mCustomView = null;
            mTargetView.setVisibility(View.GONE);
            mCustomViewCallback.onCustomViewHidden();
            mContentView.setVisibility(View.VISIBLE);
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            final JsResult finalRes = result;
            new AlertDialog.Builder(view.getContext())
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok,
                            new AlertDialog.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finalRes.confirm();
                                }
                            })
                    .setCancelable(false)
                    .create()
                    .show();
            return true;
        }
    }

    public void remove() {
        getActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .remove(this).commit();
    }

    private void removeFramesAndBlockIfBadContents() {
        //FIXME
//
//        if(!isTrustUrl(webView.getUrl())) {
//            removeFrames();
//            webView.loadUrl("javascript:window.HTMLOUT.processHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
//        }
    }

    public void changeUrl(boolean check) {
        if (check) {
            removeFramesAndBlockIfBadContents();
        }
        if (webView.getUrl() == null) return;

        if (!urlText.getText().toString().equals(webView.getUrl())) {
            removeFramesAndBlockIfBadContents();
            setUrlText(webView.getUrl());
        }

        if (mOnUrlChanged != null) {
            mOnUrlChanged.onUrlChanged(webView.getUrl());
        }
    }

    private void htmlCheck(String html) {
        String badHtml = getInternetService().hasBadHtml(html);
        if (badHtml != null) {
            toastShow("html blocking\n" + badHtml);
            blocking(badHtml);
        }
    }

    private void showDeveloperMode() {
        webView.loadUrl("javascript:(function(){naming(document.querySelectorAll('div'));function naming(element){for(var i=0;i<element.length;i++){if(element[i].querySelectorAll('div').length==0){var temp=\"\";temp+='#'+element[i].id;for(var j=0;j<element[i].classList.length;j++){temp+=\", .\"+element[i].classList[j]}element[i].innerHTML=temp;element[i].addEventListener('click',function(){this.parentNode.innerHTML='';naming(this.parentNode)})}}}})();");
    }

    private void showSource() {
        webView.loadUrl("javascript:(function(){document.body.innerText=document.getElementsByTagName('html')[0].innerHTML})();");
    }
//
//    private void removePersonalTag() {
//        try {
//            //target|urlIncluding|html/child
//            String script = "";
//            for (BlockVo caution : cautionList) {
//                String[] splited = caution.value.split("\\|");
//                if (splited.length == 3) {
//                    if (webView.getUrl().contains(splited[1])) {
//                        Pattern pattern = Pattern.compile("^[0-9a-zA-Z\\-\\_\\+\\.\\#]+$");
//                        if (pattern.matcher(splited[0]).find() && pattern.matcher(splited[2]).find()) {
//                            script += "remove(document.querySelectorAll('" + splited[0] + "'),'" + splited[2] + "');";
//                        }
//                    }
//                }
//            }
//            if (!script.isEmpty()) {
//                webView.loadUrl("javascript:(function(){" + script + "function remove(a,b){if(typeof a.length=='undefined'){if(b=='child'){a.parentNode.removeChild(a)}else{a.innerHTML=\"\"}}else{if(b=='child'){for(var i=0;i<a.length;i++){a[i].parentNode.removeChild(a[i])}}else{for(var i=0;i<a.length;i++){a[i].innerHTML=\"\"}}}}})();");
//            }
//        } catch (Exception exc) {
//            Log.d("script", "error");
//            exc.printStackTrace();
//        }
//
//    }

    private void removeFrames() {
//        removePersonalTag();
        webView.loadUrl("javascript:(function(){removeTag(\"iframe\");removeTag(\"frame\");function removeTag(tagName){var iframes=document.getElementsByTagName(tagName);for(var i=0 ; i<iframes.length ; i++){var p=document.createElement(\"p\");var link=document.createElement(\"a\");var text=document.createTextNode(iframes[i].src);link.href=iframes[i].src;link.appendChild(text);p.appendChild(link);document.body.appendChild(p);iframes[i].parentNode.replaceChild(p,iframes[i]);};}})();");
    }

    private void blocking(String reason) {
        try {
            if (mCustomView != null) {
                mClient.onHideCustomView();
            }
            webView.loadUrl("javascript:(function(){" +
                    "document.getElementsByTagName('html')[0].innerHTML='blocked';" +
                    "})();");
            //webView.loadData("BLOCKED", "text/html", "UTF-8");
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    public class MyWebViewClient extends WebViewClient {
        String previousUrl;

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            Log.d("url_override", url);
            if (url.startsWith("http") || url.startsWith("about") || url.startsWith("javascript")) {
                view.loadUrl(url);
                basicUrl = url;
            } else if (url.startsWith("intent://")) {
                try {
                    Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                    Intent existPackage = getActivity().getPackageManager().getLaunchIntentForPackage(intent.getPackage());
                    if (existPackage != null) {
                        startActivity(intent);
                    } else {
                        Intent marketIntent = new Intent(Intent.ACTION_VIEW);
                        marketIntent.setData(Uri.parse("market://details?id=" + intent.getPackage()));
                        startActivity(marketIntent);
                    }
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (url.startsWith("market://")) {
                try {
                    Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                    if (intent != null) {
                        startActivity(intent);
                    }
                    return true;
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            } else {
                boolean override = false;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.putExtra(Browser.EXTRA_APPLICATION_ID, getActivity().getPackageName());
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException ex) {
                    Log.d("url", "Not Fouund");
                }
                return override;
            }

            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d("on_page_start", url);
            progressBar.setVisibility(View.VISIBLE);
            changeUrl(false);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            progressBar.setVisibility(View.GONE);
            if ("about:blank".equals(url) && previousUrl != null) {
                view.loadUrl(previousUrl);
            } else if (url.startsWith("http")) {
                previousUrl = url;
            } else {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.putExtra(Browser.EXTRA_APPLICATION_ID, getActivity().getPackageName());
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException ex) {
                    Log.d("url", "Not Fouund");
                }

            }
            changeUrl(true);
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            changeUrl(false);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view,
                                                          String url) {
            InternetService internetService = getInternetService();
            if (internetService.isTrustUrl(url)) return null;
            if (!internetService.isWhiteUrl(url) || internetService.hasBadUrl(url) != null) {
                return new WebResourceResponse("text/html", "UTF-8",
                        new ByteArrayInputStream(url.getBytes()));
            }
            removeFramesAndBlockIfBadContents();
            return null;
        }
    }

    class LoadListener {
        @JavascriptInterface
        public void processHTML(String html) {
            htmlCheck(html);
        }
    }

    private void messageBox(String title, String msg) {
        new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        // Some stuff to do when ok got clicked
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        // Some stuff to do when cancel got clicked
                    }
                })
                .show();
    }

    private void toastShow(String str) {
        Toast.makeText(getActivity().getApplicationContext(), str,
                Toast.LENGTH_SHORT).show();
    }

}
