// Builds a SQL query that expands a ConceptSetExpression into a flat set of concept_ids.
// Equivalent to CIRCE's ConceptSetExpressionQueryBuilder.buildExpressionQuery() for
// the vocabulary resolution use case (no temporal/occurrence logic needed here).
export function buildConceptSetExpressionSql (expression, vocabSchema) {
  const items = (expression && expression.items) || []
  if (items.length === 0) return 'SELECT NULL AS concept_id WHERE 1=0'

  const includedItems = items.filter(i => !i.isExcluded)
  const excludedItems = items.filter(i => i.isExcluded)

  function itemParts (item) {
    const id = Number(item.concept.CONCEPT_ID)
    const parts = [`SELECT ${id} AS concept_id`]
    if (item.includeDescendants) {
      parts.push(
        `SELECT descendant_concept_id AS concept_id ` +
        `FROM ${vocabSchema}.concept_ancestor ` +
        `WHERE ancestor_concept_id = ${id}`
      )
    }
    if (item.includeMapped) {
      parts.push(
        `SELECT cr.concept_id_1 AS concept_id ` +
        `FROM ${vocabSchema}.concept_relationship cr ` +
        `WHERE cr.concept_id_2 = ${id} ` +
        `AND cr.relationship_id = 'Maps to' ` +
        `AND cr.invalid_reason IS NULL`
      )
    }
    return parts
  }

  const includeParts = includedItems.flatMap(itemParts)
  let sql = `SELECT DISTINCT I.concept_id FROM (\n  ${includeParts.join('\n  UNION\n  ')}\n) I`

  if (excludedItems.length > 0) {
    const excludeParts = excludedItems.flatMap(itemParts)
    sql =
      `SELECT I.concept_id FROM (${sql}) I\n` +
      `WHERE I.concept_id NOT IN (\n` +
      `  SELECT concept_id FROM (\n  ${excludeParts.join('\n  UNION\n  ')}\n  ) E\n)`
  }

  return sql
}
