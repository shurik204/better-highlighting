# Better Highlighting
Improved syntax highlighting for Minecraft commands with theme support using TextMate.

[![Fabric API](https://img.shields.io/badge/Fabric_API-%23%236A6A6A?style=for-the-badge&logoColor=%23595959&label=Requires&color=%23B1AA99)](https://modrinth.com/mod/fabric-api)
[![Modrinth](https://img.shields.io/modrinth/dt/rjsZCeTS?style=for-the-badge&logo=modrinth&logoColor=%23FFFFFF&label=%20Modrinth&labelColor=%23479152&color=%23479152)](https://modrinth.com/mod/better-highlighting)
[![Curseforge](https://img.shields.io/curseforge/dt/1032169?style=for-the-badge&logo=curseforge&logoColor=%23FFFFFF&label=%20Curseforge&labelColor=%23E16E38&color=%23E16E38)](https://www.curseforge.com/minecraft/mc-mods/better-highlighting)

### Features
- Better syntax highlighting for Minecraft commands.
- Custom theme and grammar support with resource packs.
- Bracket pair colorization.
- API for other mods to use.

### Switching themes
To switch themes, type `/betterhighlighting theme <theme>` in the chat. Command should suggest available themes.

### Included themes
- `Ayu Dark`. License: MIT | [Repository](https://github.com/ayu-theme/vscode-ayu)
- `Catppuccin Frappe`. License: MIT | [Repository](https://github.com/catppuccin/vscode)
- `Dark Plus`. License: MIT | [Repository](https://github.com/microsoft/vscode)
- `Dracula`. License: MIT | [Repository](https://github.com/dracula/visual-studio-code)
- `GitHub Dark`. License: MIT | [Repository](https://github.com/primer/github-vscode-theme)
- `Houston`. License: MIT | [Repository](https://github.com/withastro/houston-vscode)
- `Material`. License: Apache 2.0 | [Repository](https://github.com/material-theme/vsc-material-theme)
- `Monokai`. License: MIT | [Repository](https://github.com/microsoft/vscode)

## Using the API
To use the API, add the following repository to your `build.gradle`:
```gradle
maven {
    url "https://maven.shurik.me/releases"
}
```
Then add this mod as a dependency:
```gradle
modImplementation "me.shurik:better-highlighting:<version>"
``` 
Replace `<version>` with the version you want to use. You can view the list of versions [here](https://maven.shurik.me/#/releases/me/shurik/better-highlighting).

### Credits
This mod uses TextMate grammar library [TM4E](https://github.com/eclipse/tm4e) and a slightly modified version of [syntax-mcfunction](https://github.com/MinecraftCommands/syntax-mcfunction) grammar.