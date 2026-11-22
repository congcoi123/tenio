/*
The MIT License

Copyright (c) 2016-2026 kong <congcoi123@gmail.com>

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

package com.tenio.core.admin;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Usage and routing for built-in inspection commands.
 */
final class AdminCommands {

    private record Command(String usage, String description) {
    }

    private static final List<Command> COMMANDS = List.of(
            new Command("stats", "Refresh the statistics table."),
            new Command("room <id>", "Show room details and up to 50 members. Use room <id> all for the full table."),
            new Command("session <id>", "Show connection and queue details. Example: session 7"),
            new Command("network pressure", "Show up to 50 queues at least 80% full. Use network pressure all for the full table."),
            new Command("limits", "Show up to 50 rate-limit counters. Use limits all for the full table."),
            new Command("history [id]", "Show up to 50 recent events. Use history all or history <id> all for the full table. Requires history."),
            new Command("clear", "Clear the local console output."));

    private AdminCommands() {
    }

    static String shortcuts() {
        return "help | " + COMMANDS.stream().map(Command::usage).collect(Collectors.joining(" | "));
    }

    static InspectionResult help() {
        return new InspectionResult(COMMANDS.stream().map(Command::usage).toList(),
                COMMANDS.stream().map(Command::description).toList());
    }

    static boolean handles(String input) {
        String normalized = normalize(input);
        return opensFullList(normalized) || normalized.equals("server stats") || COMMANDS.stream().anyMatch(command -> {
            String name = command.usage().split(" [<\\[]", 2)[0];
            return normalized.equals(name) || normalized.startsWith(name + " ");
        });
    }

    static boolean opensFullList(String input) {
        return input.equals("help all") || input.equals("network pressure all") || input.equals("limits all")
                || input.equals("history all") || input.equals("info player all") || input.equals("info room all")
                || input.matches("(room|history) [^ ]+ all");
    }

    static String normalize(String input) {
        return input.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
