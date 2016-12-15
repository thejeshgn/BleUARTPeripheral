package thejeshgn.com.bleuartperipheral.log;

import no.nordicsemi.android.log.localprovider.LocalLogContentProvider;
import android.net.Uri;

/**
 * Created by thej on 11/12/16.
 */

public class LogProvider extends LocalLogContentProvider {
    /** The authority for the contacts provider. */
    public static final String AUTHORITY = "thejeshgn.com.bleuartperipheral";
    /** A content:// style uri to the authority for the log provider. */
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    @Override
    protected Uri getAuthorityUri() {
        return AUTHORITY_URI;
    }
}
