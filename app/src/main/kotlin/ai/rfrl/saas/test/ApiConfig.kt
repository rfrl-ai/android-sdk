package ai.rfrl.saas.test

import androidx.annotation.StringRes
import com.pocketimps.extlib.Tagged


enum class ApiConfig(override val tag: String,
                     @StringRes val urlResId: Int,
                     @StringRes val tokenResId: Int)
         : Tagged {
  RELEASE("release", R.string.api_url, R.string.token_rfrl),
  DEV("dev", R.string.api_url_dev, R.string.token_rfrl_dev)
}
