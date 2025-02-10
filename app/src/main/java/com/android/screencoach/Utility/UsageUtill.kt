package com.android.achievix.Utility

import android.annotation.SuppressLint
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.android.achievix.Database.BlockDatabasee
import com.android.achievix.Model.AppBlockModell
import com.android.achievix.Model.AppSelectModell
import com.android.achievix.Model.AppUsageModell
import com.android.achievix.R
import java.io.ByteArrayOutputStream
import java.util.Calendar

class UsageUtill {
    companion object {
        @JvmField
        var totalUsage: Long = 0

        @SuppressLint("ServiceCast", "QueryPermissionsNeeded", "UseCompatLoadingForDrawables")
        fun getInstalledAppsUsage(context: Context, sort: String?): List<AppUsageModell> {
            totalUsage = 0

            val usageTimes = getUsageTimes(context, sort)
            val totalUsageTime = usageTimes.values.sum()

            return getAppsList(context).mapNotNull { app ->
                val usageTime = usageTimes.getOrDefault(app.packageName, 0L)
                if (usageTime != 0L) {
                    totalUsage += usageTime
                    val usagePercentage = usageTime.toDouble() / totalUsageTime * 100
                    val appName = app.loadLabel(context.packageManager).toString()
                    val icon = getIcon(context, app)
                    AppUsageModell(appName, app.packageName, icon, usageTime.toString(), usagePercentage)
                } else {
                    null
                }
            }.sortedByDescending { it.progress ?: Double.MIN_VALUE }
        }

        fun getInstalledAppsSelect(context: Context): List<AppSelectModell> {
            return getAppsList(context).map { app ->
                val appName = app.loadLabel(context.packageManager).toString()
                val icon = getIcon(context, app)
                AppSelectModell(appName, app.packageName, icon, false)
            }
        }

        @SuppressLint("ServiceCast", "QueryPermissionsNeeded", "UseCompatLoadingForDrawables")
        fun getInstalledAppsBlock(context: Context, sort: String, caller: String): List<AppBlockModell> {
            val usageTimes = getUsageTimes(context, "Daily")
            val networkUsageMap = getNetworkUsageMap(context, caller)

            return getAppsList(context).mapNotNull { app ->
                val usageTime = usageTimes.getOrDefault(app.packageName, 0L)
                val appName = app.loadLabel(context.packageManager).toString()
                val icon = getIcon(context, app)

                when (caller) {
                    "InternetBlockActivity" -> {
                        val blocked = BlockDatabasee(context).isInternetBlocked(app.packageName)
                        val networkUsage = networkUsageMap[app.packageName]?.toString()?.toLongOrNull() ?: 0L
                        AppBlockModell(
                            appName = appName,
                            packageName = app.packageName,
                            iconResId = R.drawable.block,  // You need to replace 'some_icon' with actual drawable resource ID
                            extra = networkUsage,
                            blocked = blocked
                        )
                    }
                    "AppBlockActivityy" -> {
                        val blocked = BlockDatabasee(context).isAppBlocked(app.packageName)
                        val usageTimeLong = (usageTime as? String)?.toLongOrNull() ?: 0L
                        AppBlockModell(
                            appName = appName,
                            packageName = app.packageName,
                            iconResId = R.drawable.block,  // Replace 'some_icon' with your drawable resource ID
                            extra = usageTimeLong,
                            blocked = blocked
                        )
                    }
                    else -> null
                }


            }.sort(sort)
        }

        private fun getUsageTimes(context: Context, sort: String?): MutableMap<String, Long> {
            val (beginTime, endTime) = getTimeRange(sort)

            val usageStatsManager =
                context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val events = usageStatsManager.queryAndAggregateUsageStats(beginTime, endTime)
            val usageTimes: MutableMap<String, Long> = HashMap()

            for (packageName in events.keys) {
                val stats = events[packageName]
                val usageTime = stats?.totalTimeInForeground

                if (usageTime != null) {
                    usageTimes[packageName] = usageTime
                }
            }

            return usageTimes
        }

        fun getTimeRange(sort: String?): Pair<Long, Long> {
            val calendar = Calendar.getInstance()

            when (sort) {
                "Daily" -> {
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                }

                "Weekly" -> {
                    calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                }

                "Monthly" -> {
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                }

                "Yearly" -> {
                    calendar.set(Calendar.DAY_OF_YEAR, 1)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                }
            }

            val beginTime = calendar.timeInMillis
            val endTime = System.currentTimeMillis()

            return Pair(beginTime, endTime)
        }

        @SuppressLint("QueryPermissionsNeeded")
        private fun getAppsList(context: Context): List<ApplicationInfo> {
            val applicationInfo =  context.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            return applicationInfo.filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 || it.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0 }
        }

        private fun getIcon(context: Context, app: ApplicationInfo): Drawable {
            val icon = app.loadIcon(context.packageManager)
            val bitmap: Bitmap = if (icon is BitmapDrawable) {
                icon.bitmap
            } else {
                if (icon.intrinsicWidth <= 0 || icon.intrinsicHeight <= 0) {
                    Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                } else {
                    Bitmap.createBitmap(icon.intrinsicWidth, icon.intrinsicHeight, Bitmap.Config.ARGB_8888)
                }.also {
                    val canvas = Canvas(it)
                    icon.setBounds(0, 0, canvas.width, canvas.height)
                    icon.draw(canvas)
                }
            }

            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream)

            val bitmapData = stream.toByteArray()
            return BitmapDrawable(context.resources, BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.size))
        }

        private fun getNetworkUsageMap(
            context: Context,
            caller: String
        ): HashMap<String, Float> {
            val networkUsageMap: HashMap<String, Float> = HashMap()
            val (beginTime, endTime) = getTimeRange("Daily")

            val networkUtil = NetworkUtill()
            if (caller == "InternetBlockActivity") {
                for (app in getAppsList(context)) {
                    if (app.flags and ApplicationInfo.FLAG_SYSTEM == 0 || app.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0) {
                        networkUsageMap[app.packageName] = networkUtil.getPackageInfo(
                            beginTime,
                            endTime,
                            app.packageName,
                            true,
                            true,
                            context
                        )
                    }
                }
            }

            return networkUsageMap
        }

        private fun List<AppBlockModell>.sort(sort: String): List<AppBlockModell> {
            return when (sort) {
                "Name" -> this.sortedBy { it.appName }
                "Usage" -> this.sortedByDescending { it.extra.toDouble() }
                "Blocked" -> this.sortedByDescending { it.blocked }
                else -> this.sortedBy { it.appName }
            }
        }
    }

    fun getUsageByPackageName(context: Context, packageName: String, sort: String): Long {
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        when (sort) {
            "Daily" -> {
                val (beginTime, endTime) = getTimeRange(sort)
                val events = usageStatsManager.queryAndAggregateUsageStats(beginTime, endTime)
                val stats = events[packageName]
                return stats?.totalTimeInForeground ?: 0
            }

            "Weekly" -> {
                val (beginTime, endTime) = getTimeRange(sort)
                val events = usageStatsManager.queryAndAggregateUsageStats(beginTime, endTime)
                val stats = events[packageName]
                return stats?.totalTimeInForeground ?: 0
            }

            "Monthly" -> {
                val (beginTime, endTime) = getTimeRange(sort)
                val events = usageStatsManager.queryAndAggregateUsageStats(beginTime, endTime)
                val stats = events[packageName]
                return stats?.totalTimeInForeground ?: 0
            }

            "Yearly" -> {
                val (beginTime, endTime) = getTimeRange(sort)
                val events = usageStatsManager.queryAndAggregateUsageStats(beginTime, endTime)
                val stats = events[packageName]
                return stats?.totalTimeInForeground ?: 0
            }
        }

        return 0
    }

    fun getUsageByPackageNameAndMillis(
        context: Context,
        packageName: String,
        beginTime: Long,
        endTime: Long
    ): Long {
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val events = usageStatsManager.queryAndAggregateUsageStats(beginTime, endTime)
        val stats = events[packageName]
        return stats?.totalTimeInForeground ?: 0
    }
}