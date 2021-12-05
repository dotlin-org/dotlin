Note that any custom IR elements must be handled in 
`IrCustomElementHelper` ([`../IrCustomElementsUtils.kt`](../IrCustomElementUtils.kt)),
and then in `DeepCopier` and `DeclarationReferenceRemapper` ([`../DeepCopy.kt`](../DeepCopy.kt))