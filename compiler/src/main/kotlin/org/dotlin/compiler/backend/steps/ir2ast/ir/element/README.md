Note that any custom IR elements must be handled in 
- `IrCustomElementHelper` ([`../IrCustomElementsUtils.kt`](../IrCustomElementUtils.kt))
- `DeepCopier` ([`../DeepCopy.kt`](../DeepCopy.kt))
- If not lowered out: `IrToDartExpressionTransformer` ([`../transformer/IrToDartExpression.kt`](../transformer/IrToDartExpression.kt))
- Possibly `DeclarationReferenceRemapper` ([`../DeepCopy.kt`](../DeepCopy.kt))
