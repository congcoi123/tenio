/*
The MIT License

Copyright (c) 2016-2025 kong <congcoi123@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package com.tenio.core.configuration.setting;

/**
 * A mapping class which references to this {@code setting.json} file.
 *
 * @since 0.4.0
 */
public final class Setting {

  private Command command;
  private Plugin plugin;

  /**
   * Retrieves the command section.
   *
   * @return an instance of {@link Command}
   */
  public Command getCommand() {
    return command;
  }

  /**
   * Sets the command sections.
   *
   * @param command the instance of {@link Command} references to its settings
   */
  public void setCommand(Command command) {
    this.command = command;
  }

  /**
   * Retrieves the plugin section.
   *
   * @return an instance of {@link Plugin}
   */
  public Plugin getPlugin() {
    return plugin;
  }

  /**
   * Sets the plugin sections.
   *
   * @param plugin the instance of {@link Plugin} references to its settings
   */
  public void setPlugin(Plugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public String toString() {
    return "Setting{" +
        "command=" + command +
        ", plugin=" + plugin +
        '}';
  }

  /**
   * A mapping class which references to this {@code setting.json} file.
   */
  public final static class Command {

    private boolean enabled;

    /**
     * Checks whether this command is enabled.
     *
     * @return {@code true} if the command is enabled, otherwise, returns {@code false}
     */
    public boolean isEnabled() {
      return enabled;
    }

    /**
     * Setups the command.
     *
     * @param enabled sets its value to {@code true} to enable the command, otherwise, put the
     *                value {@code false}
     */
    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    @Override
    public String toString() {
      return "SystemCommand{" +
          "enabled=" + enabled +
          '}';
    }
  }

  /**
   * A mapping class which references to this {@code setting.json} file.
   */
  public final static class Plugin {

    private boolean enabled;
    private String path;

    /**
     * Checks whether this command is enabled.
     *
     * @return {@code true} if the command is enabled, otherwise, returns {@code false}
     */
    public boolean isEnabled() {
      return enabled;
    }

    /**
     * Setups the command.
     *
     * @param enabled sets its value to {@code true} to enable the command, otherwise, put the
     *                value {@code false}
     */
    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    /**
     * Retrieves the plugin executable file's path.
     *
     * @return the plugin path
     */
    public String getPath() {
      return path;
    }

    /**
     * Sets the plugin executable file's path.
     *
     * @param path the file's path
     */
    public void setPath(String path) {
      this.path = path;
    }

    @Override
    public String toString() {
      return "Plugin{" +
          "enabled=" + enabled +
          ", path='" + path + '\'' +
          '}';
    }
  }
}
