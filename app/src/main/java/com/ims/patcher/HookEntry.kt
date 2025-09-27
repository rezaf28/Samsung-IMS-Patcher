package com.ims.patcher

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.param.HookParam
import com.highcapable.yukihookapi.hook.factory.*
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit

@InjectYukiHookWithXposed
object HookEntry : IYukiHookXposedInit {

    private val TARGETS = setOf("globalsettings", "imsprofile", "imsswitch", "mnomap")
    private const val MODULE_PKG = "com.ims.patcher"
    private val reentrant = ThreadLocal.withInitial { false }

    override fun onInit() = YukiHookAPI.configs {
        isDebug = false
    }

    override fun onHook() = YukiHookAPI.encase {
        loadApp(name = "com.sec.imsservice") {
            "android.content.res.Resources".toClass().apply {
                method {
                    name = "openRawResource"
                    param(IntType)
                }.hook {
                    before { serveRaw(this) }
                }
                method {
                    name = "openRawResource"
                    param(IntType, TypedValue::class.java)
                }.hook {
                    before { serveRaw(this) }
                }
                method {
                    name = "openRawResourceFd"
                    param(IntType)
                }.hook {
                    before { serveRawFd(this) }
                }
            }
        }
    }

    private fun serveRaw(param: HookParam) {
        if (reentrant.get()) {
            try { param.callOriginal() } catch (_: Throwable) {}
            return
        }
        try {
            val id = param.args.firstOrNull() as? Int ?: return
            val res = param.instanceOrNull as? Resources ?: return
            val entry = runCatching { res.getResourceEntryName(id) }.getOrNull() ?: return
            if (entry !in TARGETS) return
            val mc = moduleContext() ?: return
            val mid = mc.resources.getIdentifier(entry, "raw", MODULE_PKG)
            if (mid == 0) return
            reentrant.set(true)
            param.result = mc.resources.openRawResource(mid)
        } catch (_: Throwable) {
            try { param.callOriginal() } catch (_: Throwable) {}
        } finally {
            reentrant.set(false)
        }
    }

    private fun serveRawFd(param: HookParam) {
        if (reentrant.get()) {
            try { param.callOriginal() } catch (_: Throwable) {}
            return
        }
        try {
            val id = param.args.firstOrNull() as? Int ?: return
            val res = param.instanceOrNull as? Resources ?: return
            val entry = runCatching { res.getResourceEntryName(id) }.getOrNull() ?: return
            if (entry !in TARGETS) return
            val mc = moduleContext() ?: return
            val mid = mc.resources.getIdentifier(entry, "raw", MODULE_PKG)
            if (mid == 0) return
            reentrant.set(true)
            param.result = mc.resources.openRawResourceFd(mid)
        } catch (_: Throwable) {
            try { param.callOriginal() } catch (_: Throwable) {}
        } finally {
            reentrant.set(false)
        }
    }

    private fun moduleContext(): Context? =
        runCatching {
            val at = Class.forName("android.app.ActivityThread")
            val app = at.getMethod("currentApplication").invoke(null) as Context
            app.createPackageContext(MODULE_PKG, Context.CONTEXT_IGNORE_SECURITY)
        }.getOrNull()
}
