## CHECK LIST

### Before creating a new Pull Request (PR)
- [x] Implements test cases and make sure the *Coverage* covers more than 50% the source code  
- [x] Writes *java-docs* for every classes, public methods, enumeration values, open-module, important variables  
- [x] Executes command *mvn clean install* to run the *checkstyle* process  

### Before updating to new version
- [x] Makes sure the current branch is *develop* branch  
- [x] All new methods should be annotated by the *@since* annotation  
- [x] Changes version in *pom.xml* file  
- [x] Updates the class *TradeMark.java* in the *tenio-game* module  
- [x] Updates new changes in the *CHANGELOG.md* file  
- [x] Updates the *VERSION* file  
- [x] Updates the *README.md* file  
- [x] Executes command *mvn clean install* to run the *checkstyle* process  
- [x] Creates a Pull Request (PR) from develop to master branch  

### Creating a new release
- [x] Makes sure the current branch is *master* branch  
- [x] Creates new *release* branch using pattern name likes *tenio-game-0.0.2.20211016*  

### Deploying a release to mvn repositories center
- [x] Makes sure the current branch is right *release* branch  
- [x] Execute commands to deploy the package  
