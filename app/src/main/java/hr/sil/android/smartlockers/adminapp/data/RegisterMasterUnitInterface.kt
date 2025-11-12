package hr.sil.android.smartlockers.adminapp.data

import hr.sil.android.smartlockers.adminapp.core.remote.model.EPaperType
import hr.sil.android.smartlockers.adminapp.core.remote.model.SplType


interface RegisterMasterUnitInterface {

   fun chooseSplKeyboard(splType: SplType)

   fun chooseEpaperType(ePaperType: EPaperType)

}