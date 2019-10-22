package com.instana.idea.php.util;

import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.php.composer.InstalledPackageData;
import com.jetbrains.php.config.library.PhpLibraryRoot;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class PhpUtil {

    public static final String INSTANA_SDK_COMPOSER_NAME = "instana/instana-php-sdk";

    public static Stream<PhpLibraryRoot> getNonRuntimeLibraryRoots() {
        return Arrays.stream(PhpLibraryRoot.EP_NAME.getExtensions()).filter(ex -> !ex.runtime);
    }

    public static boolean containsSdkPackage(@NotNull List<InstalledPackageData> installedPackagesFromConfig) {
        return ContainerUtil.exists(installedPackagesFromConfig, x -> x.getName().equals(INSTANA_SDK_COMPOSER_NAME));
    }

    public static boolean fqnBelongsToPhpSDK(String fqn) {
        return fqn.equals("\\Instana\\Tracer") || fqn.equals("\\Instana\\Span") || fqn.equals("\\Instana\\InstanaRuntimeException");
    }
}
