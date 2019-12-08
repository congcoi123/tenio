package com.tenio.engine.entitas.api;

/**
 * <p>
 * Implement this interface if you want to create a component which you can add to an entity.<br>
 * Optionally, you can add these annotations:<br>
 * <b>'@Unique':</b> the code generator will generate additional methods for the context to ensure that only one entity with this component exists.<br>
 * <tt>E.g. context.isAnimating = true or context.setResources();</tt><br>
 * <b>'@Contexts':</b> You can make this component to be available only in the specified contexts.<br>
 * The code generator can generate these attributes for you.<br>
 * More available Annotations can be found in <tt>entitas.codeGenerator.annotations.</tt>
 * </p>
 * @author Rubentxu
 */
public interface IComponent {
	
}
