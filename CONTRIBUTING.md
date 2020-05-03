# Contributing
We are happy with your help in making the `TenIO` project better. Please read the guidelines for contributing bug reports and new code, or it might be hard for the community to help you with your issue or pull request.

## Project roles
### Reviewer
If you having submit a pull request, you can assign a reviewer is any members below to review and merge your pull request.
- [Kong](https://github.com/congcoi123)

### Core Committer
The contributors with commit access and release to `TenIO` project. If you want to become a core committer please start writing Pull Requests.
- [Kong](https://github.com/congcoi123)

## Submitting pull requests
Once you've come up with a good design, go ahead and submit a pull request (PR). When submitting a PR, please follow these guidelines:
- Base all your work off of the `develop` branch. The `develop` branch is where active development happens. 
	- **Note:** We do not merge pull requests directly into master.
- Limit yourself to one feature or bug fix per pull request.
- Should include tests that prove your code works.
- Follow appropriate style for code contributions and commit messages.
- Be sure your author field in git is properly filled out with your full name and email address so we can credit you.

## Commit conventions
`TenIO` is using [conventional commits](https://www.conventionalcommits.org), you can enter commit with the message:
```sh
type(scope): commit message
```

Example:
```sh
$ git commit -m "fix: can not found the configuration file"
```

##### Allowed `type` values:
- **feat** - new feature, not a new feature for build script.
- **fix** - bug fix, not a fix to a build script.
- **docs** - changes to the documentation.
- **meta** - formatting, missing semi colons, etc; no production code change.
- **refactor** - refactoring production code, eg. renaming a variable.
- **test** - adding missing tests, refactoring tests, no production code change.
- **chore** - updating grunt tasks etc, no production code change.
- **ci** - ci configure.
- **perf** - a code change that improves performance.
