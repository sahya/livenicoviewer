package jp.android.sahya.NicoLiveViewer;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Process;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.LinearLayout;

public class FlashPlayer extends Activity
  implements ax
{
  private static FlashPlayer a;
  private static WebView b;
  private static int c;
  private static int d;
  private static int f;
  private static int g;
  private static ay m;
  private static ProgressDialog o;
  private int e;
  private fg h;
  private boolean[] i;
  private int[] j;
  private Intent k;
  private boolean l = true;
  private au n;
  private LinearLayout p;
  private EditText q;
  private boolean r;
  private BroadcastReceiver s;
  private cs t;
  private String u;
  private View v;
  private boolean w;
  private Intent x;

  public static FlashPlayer a()
  {
    return a;
  }

  public static void a(ay paramay)
  {
    if (o != null)
      o.dismiss();
    m = paramay;
  }

  private static void m()
  {
    if (b != null)
    {
      b.stopLoading();
      b.setWebChromeClient(null);
      b.setWebViewClient(null);
      b.destroyDrawingCache();
      b = null;
    }
  }

  public final void a(int paramInt)
  {
    if (this.t != null);
    switch (paramInt)
    {
    case 0:
      setRequestedOrientation(-1);
      getIntent().putExtra("fix_screen", 0);
      break;
    case 1:
      setRequestedOrientation(1);
      getIntent().putExtra("fix_screen", 1);
      break;
    case 2:
      setRequestedOrientation(0);
      getIntent().putExtra("fix_screen", 2);
    }
  }

  public final void a(at paramat, String paramString)
  {
    this.n.a(paramat, paramString);
  }

  public final void b()
  {
    Intent localIntent = new Intent();
    try
    {
      localIntent.putExtra("cookie", CookieManager.getInstance().getCookie("nicovideo.jp"));
      localIntent.putExtra("x_pos", this.j[7]);
      localIntent.putExtra("y_pos", this.j[8]);
      localIntent.putExtra("bottom_pos", this.j[9]);
      int i1 = ((AudioManager)getSystemService("audio")).getRingerMode();
      if ((i1 == 1) || (i1 == 0))
        localIntent.putExtra("audiovolume", String.valueOf(getIntent().getIntExtra("audiovolume", -1)));
      setResult(88, localIntent);
      m();
      finish();
      return;
    }
    catch (IllegalStateException localIllegalStateException)
    {
      while (true)
      {
        if (this.t != null);
        fx.a(a, "Ý’è‚Ìˆø‚«Œp‚¬‚ÉŽ¸”s‚µ‚Ü‚µ‚½B");
      }
    }
    catch (NullPointerException localNullPointerException)
    {
      while (true)
        fx.a(a, "Ý’è‚Ìˆø‚«Œp‚¬‚ÉŽ¸”s‚µ‚Ü‚µ‚½B");
    }
  }

  public final void c()
  {
    boolean bool = false;
    getIntent();
    if (b == null)
    {
      fx.a(a, "ƒvƒŒƒCƒ„[‚ª“Ç‚Ýž‚Ü‚ê‚Ä‚¢‚Ü‚¹‚ñ");
    }
    else
    {
      if (!getIntent().getBooleanExtra("sp_player", false))
        bool = true;
      getIntent().putExtra("sp_player", bool);
      if (!bool)
        b.loadDataWithBaseURL(jh.d + this.h.d(), jh.n.replace("%LIVEID%", this.h.d()), "text/html", "utf-8", null);
      else
        b.loadDataWithBaseURL(jh.e + this.h.d(), jh.H.replace("%LIVEID%", this.h.d()), "text/html", "utf-8", null);
    }
  }

  public boolean dispatchKeyEvent(KeyEvent paramKeyEvent)
  {
    boolean bool = true;
    if (!getIntent().getBooleanExtra("only_comment", bool))
    {
      if (this.r)
      {
        this.r = false;
        break label112;
      }
    }
    else
      finish();
    if (paramKeyEvent.getKeyCode() != 4)
      bool = super.dispatchKeyEvent(paramKeyEvent);
    else if ((this.q == null) || (!this.q.isFocused()))
    {
      if (!getIntent().getBooleanExtra("fexit", bool))
        b();
      else
        new cx(this).show();
    }
    else
      this.q.clearFocus();
    label112: return bool;
  }

  public void finish()
  {
    if (this.t != null);
    CookieSyncManager.getInstance().stopSync();
    if (this.s != null)
      unregisterReceiver(this.s);
    this.l = false;
    m();
    System.gc();
    super.finish();
    Process.sendSignal(Process.myPid(), 9);
  }

  public final au g()
  {
    return this.n;
  }

  public final void n()
  {
    new am(this, this.h.z()).show();
  }

  protected void onActivityResult(int paramInt1, int paramInt2, Intent paramIntent)
  {
    int i1 = 0;
    if (this.t == null)
    {
      setResult(666);
      finish();
    }
    if (paramInt1 == 2222)
      this.r = true;
    Object localObject;
    if (paramInt2 == 88)
    {
      if (paramIntent != null)
      {
        this.j[7] = paramIntent.getIntExtra("x_pos", this.j[7]);
        this.j[8] = paramIntent.getIntExtra("y_pos", this.j[8]);
        this.j[9] = paramIntent.getIntExtra("bottom_pos", this.j[9]);
        this.n = ((au)paramIntent.getSerializableExtra("cmd"));
        if (this.n == null)
        {
          getApplicationContext();
          localObject = new String[4];
          localObject[i1] = flashplayerwith.a("cmd_cmd");
          localObject[1] = flashplayerwith.a("cmd_size");
          localObject[2] = flashplayerwith.a("cmd_color");
          localObject[3] = flashplayerwith.a("cmd_align");
        }
      }
      for (i1 = 0; ; i1++)
      {
        if (i1 >= 4);
        while (true)
        {
          new dc(this).execute(new Void[0]);
          return;
          if ((localObject[i1] == null) || (i1 != 3))
            break;
          this.n = new au(localObject[0], localObject[1], localObject[2], localObject[3], String.valueOf(this.h.z()));
        }
        if (i1 != 3)
          continue;
        this.n = new au();
        this.n.a(at.g, String.valueOf(this.h.z()));
      }
    }
    int i3;
    int i2;
    if (paramInt2 == 8888)
    {
      i3 = 100;
      if (paramIntent == null)
        break label639;
      i1 = paramIntent.getIntExtra("x_pos", this.j[7]);
      i2 = paramIntent.getIntExtra("y_pos", this.j[8]);
      i3 = paramIntent.getIntExtra("bottom_pos", this.j[9]);
    }
    while (true)
    {
      while (true)
      {
        localObject = new Intent();
        try
        {
          ((Intent)localObject).putExtra("x_pos", i1);
          ((Intent)localObject).putExtra("y_pos", i2);
          ((Intent)localObject).putExtra("bottom_pos", i3);
          ((Intent)localObject).putExtra("cookie", "");
          setResult(8888, (Intent)localObject);
          m();
          finish();
        }
        catch (IllegalStateException localIllegalStateException)
        {
        }
      }
      if (this.t == null)
        break;
      break;
      if (paramInt2 == 9999)
      {
        i1 = paramIntent.getIntExtra("overlay_error", 0);
        if (this.t != null)
        {
          this.t.a(i1);
          this.t.a();
          localObject = new Intent();
          ((Intent)localObject).putExtra("flash_error", i1);
          setResult(7777, (Intent)localObject);
          new dc(this).execute(new Void[0]);
          break;
        }
        finish();
        break;
      }
      if (paramInt2 == 1010)
      {
        Intent localIntent = new Intent();
        localIntent.putExtra("flash_error", -18);
        setResult(7777, localIntent);
      }
      while (true)
      {
        finish();
        break;
        if (paramInt2 == 12345)
        {
          new Intent().putExtra("flash_error", -17);
          setResult(12345);
          continue;
        }
        if (paramInt2 != 7)
          break;
        if (paramIntent == null)
          continue;
        setResult(7, paramIntent);
      }
      label639: i2 = 0;
    }
  }

  public void onConfigurationChanged(Configuration paramConfiguration)
  {
    super.onConfigurationChanged(paramConfiguration);
    if (paramConfiguration.orientation != 2)
    {
      if ((paramConfiguration.orientation == 1) && (b != null))
        if (!getIntent().getBooleanExtra("sp_player", false))
        {
          b.loadUrl("javascript:document.getElementById('flvplayer').width=" + f);
          b.loadUrl("javascript:document.getElementById('flvplayer').height=" + g);
        }
        else
        {
          b.loadUrl("javascript:document.getElementById('flvplayer').width=" + c);
          b.loadUrl("javascript:document.getElementById('flvplayer').height=" + (d / 2 - this.e));
        }
    }
    else if (b != null)
    {
      if (!getIntent().getBooleanExtra("sp_player", false))
      {
        b.loadUrl("javascript:document.getElementById('flvplayer').width=" + 0.57D * g);
        b.loadUrl("javascript:document.getElementById('flvplayer').height=" + 0.53D * f);
      }
      else
      {
        b.loadUrl("javascript:document.getElementById('flvplayer').width=" + d);
        b.loadUrl("javascript:document.getElementById('flvplayer').height=" + (c - this.e));
      }
      b.setFocusable(true);
      b.clearFocus();
    }
  }

  public void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    requestWindowFeature(1);
    getWindow().setSoftInputMode(3);
    a = this;
    this.v = LayoutInflater.from(this).inflate(2130903054, null);
    setContentView(this.v);
    flashplayerwith localflashplayerwith = (flashplayerwith)getApplicationContext();
    localflashplayerwith.b();
    localflashplayerwith.a(a);
    this.t = flashplayerwith.s();
    this.x = getIntent();
    this.w = this.x.getBooleanExtra("notification", false);
    new db(this).execute(new Void[0]);
    if (!this.w)
      new df(this).execute(new Void[0]);
  }

  public boolean onCreateOptionsMenu(Menu paramMenu)
  {
    getMenuInflater().inflate(2131230720, paramMenu);
    return super.onPrepareOptionsMenu(paramMenu);
  }

  public void onDestroy()
  {
    if (this.t != null);
    super.onDestroy();
  }

  public boolean onOptionsItemSelected(MenuItem paramMenuItem)
  {
    Object localObject;
    switch (paramMenuItem.getItemId())
    {
    case 2131296465:
      if (this.p == null)
      {
        localObject = new Intent();
        ((Intent)localObject).putExtra("flash_error", -8);
        setResult(7777, (Intent)localObject);
        finish();
      }
      this.p.setVisibility(4);
      if (m != null)
        m.a();
      b.setPadding(0, 0, 0, 0);
      if (this.n != null)
        this.k.putExtra("cmd", this.n);
      localObject = getIntent();
      this.k.putExtra("fix_screen", ((Intent)localObject).getIntExtra("fix_screen", 0));
      this.k.putExtra("Cookie", this.u);
      this.k.putExtra("table_settings", this.i);
      this.k.putExtra("table_intarray", this.j);
      this.k.putExtra("viewW", ((Intent)localObject).getIntExtra("viewW", getWindowManager().getDefaultDisplay().getWidth()));
      this.k.putExtra("viewH", ((Intent)localObject).getIntExtra("viewH", getWindowManager().getDefaultDisplay().getHeight()));
      this.k.putExtra("LiveInfo", this.h);
      startActivityForResult(this.k, 2222);
      break;
    case 2131296466:
      if (this.p.getVisibility() != 4)
      {
        b.setPadding(0, 0, 0, 0);
        this.p.setVisibility(4);
      }
      else
      {
        b.setPadding(0, this.p.getHeight(), 0, 0);
        this.p.setVisibility(0);
      }
      break;
    case 2131296467:
      if (this.n == null)
      {
        this.n = new au();
        this.n.a(at.g, String.valueOf(this.h.z()));
      }
      if (this.h == null)
        break;
      localObject = m;
      new cg(this, (ay)localObject, this.h, true).show();
    }
    return true;
  }

  public void onPause()
  {
    if ((this.t == null) || (m != null))
      m.a();
    super.onPause();
    if ((this.l) && (this.l))
      onStart();
  }

  public void onResume()
  {
    if (this.t != null);
    super.onResume();
  }

  public void onWindowFocusChanged(boolean paramBoolean)
  {
    getWindow().setSoftInputMode(3);
    if (this.w)
    {
      flashplayerwith localflashplayerwith = (flashplayerwith)getApplicationContext();
      DisplayMetrics localDisplayMetrics = new DisplayMetrics();
      getWindowManager().getDefaultDisplay().getMetrics(localDisplayMetrics);
      float f1 = localDisplayMetrics.scaledDensity;
      int i3 = getWindow().getDecorView().getWidth();
      int i1 = getWindow().getDecorView().getHeight();
      if (i3 <= i1)
      {
        int i2 = i1;
        i1 = i3;
        i3 = i2;
      }
      localflashplayerwith.a(i1);
      localflashplayerwith.b(i3);
      localflashplayerwith.a(f1);
      localflashplayerwith.c((int)(1.72D * (i1 / f1)));
      localflashplayerwith.d((int)(1.8D * (i3 / f1)));
      localflashplayerwith.a(i1);
      localflashplayerwith.b(i3);
      c = (int)(i1 / f1);
      d = (int)(i3 / f1);
      this.e = (int)f1;
      this.x.putExtra("sp_player", flashplayerwith.b("sp_player"));
      this.x.putExtra("resizeW", localflashplayerwith.h());
      this.x.putExtra("resizeH", localflashplayerwith.i());
      this.x.putExtra("viewW", c);
      this.x.putExtra("viewH", d);
      this.x.putExtra("dencity", f1);
      localflashplayerwith.d();
      new df(this).execute(new Void[0]);
      this.w = false;
    }
  }

  public final void p()
  {
    String[] arrayOfString = new String[3];
    arrayOfString[0] = "’[––‚ÌÝ’è‚ð—˜—p";
    arrayOfString[1] = "cŒÅ’è";
    arrayOfString[2] = "‰¡ŒÅ’è";
    int i1 = getIntent().getIntExtra("fix_screen", 0);
    new AlertDialog.Builder(this).setTitle(arrayOfString[i1]).setItems(arrayOfString, new da(this, i1)).show();
  }
}