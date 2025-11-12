package hr.sil.android.smartlockers.adminapp.cache

import hr.sil.android.datacache.TwoLevelCache
import hr.sil.android.smartlockers.adminapp.App
import hr.sil.android.smartlockers.adminapp.data.DeliveryKey

/**
 * Created by Stef on 29.1.2018..
 */
object DatabaseHandler {


    val deliveryKeyDb by lazy {
        TwoLevelCache
                .Builder(DeliveryKey::class, DeliveryKey::masterMacAddress)
                .memoryLruMaxSize(20)
                .build(App.ref)
    }


}