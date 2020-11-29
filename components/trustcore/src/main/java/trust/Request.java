package trust;

import android.net.Uri;
import android.os.Parcelable;
import androidx.annotation.Nullable;

public interface Request extends Parcelable {

    <T> T body();

    Uri key();

    @Nullable
    Uri getCallbackUri();

}
