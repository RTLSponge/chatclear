package au.id.rleach.chatclear;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.plugin.PluginContainer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;

public final class ConfigLoader {

    private ConfigLoader() {
    }

    public static HoconConfigurationLoader getLoader(final Path configDir, final Asset asset) throws IOException {
        final String filename = new File(asset.getUrl().getPath()).getName();

        final Path savePath = configDir.resolve(filename);
        final boolean loadDefaults = !Files.exists(savePath);

        Files.createDirectories(savePath.getParent());
        final HoconConfigurationLoader.Builder defaultBuilder = HoconConfigurationLoader.builder().setURL(asset.getUrl());
        final HoconConfigurationLoader.Builder saveBuilder = HoconConfigurationLoader.builder().setPath(savePath);

        final HoconConfigurationLoader saveNode = saveBuilder.build();
        if(loadDefaults) {
            asset.copyToFile(savePath);
            final CommentedConfigurationNode defaultNode = defaultBuilder.build().load();
            saveNode.save(defaultNode);
        }
        return saveNode;
    }

    public static CommentedConfigurationNode loadConfigUnchecked(String asset, Path configDir, PluginContainer container){
        try {
            return getLoader(configDir, container.getAsset(asset).get()).load();
        } catch (IOException e) {
            throw new ConfigReadWriteException(asset, configDir, e);
        } catch (NoSuchElementException e){
            throw new MissingAssetException(asset, e);
        }
    }

    private static class MissingAssetException extends Error {
        public MissingAssetException(String asset, Throwable cause) {
            super("Missing Asset: " + asset, cause);
        }
    }

    static class ConfigReadWriteException extends RuntimeException {
        public ConfigReadWriteException(String asset, Path configDir, IOException e) {
            super("Could not read / write config from asset: "+asset+ " to config directory "+configDir.toAbsolutePath(), e);
        }
    }
}
