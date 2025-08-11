/** *****************************************************************************
 * Copyright 2025 Observational Health Data Sciences and Informatics
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************** */
package org.ohdsi.webapi.vocabulary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConceptSetCondenser {

  private Set<Long> includedConcepts;
  private List<CandidateConcept> candidateConcepts;
  private CandidateConcept.Options[] currentSolution;
  private CandidateConcept.Options[] optimalSolution;
  private int optimalLength;
  private int firstExclusionIndex;
  private List<Set<Long>> remainingConceptsAtLevel;
  private long stepCount;
  private static long MAX_STEP_COUNT = 10000000;

  /**
   * Constructor
   *
   * @param includedConcepts The set of concept IDs included in the concept set.
   * @param candidateConcepts An array of candidate concepts. A candidate
   * concept is either in the concept set, or is a descendant of a concept in
   * the concept set.
   */
  public ConceptSetCondenser(long[] includedConcepts, CandidateConcept[] candidateConcepts) {
    this.includedConcepts = new HashSet<>(includedConcepts.length);
    for (long concept : includedConcepts) {
      this.includedConcepts.add(concept);
    }
    this.candidateConcepts = new ArrayList<>(candidateConcepts.length);
    Set<Long> candidateConceptIds = new HashSet<>(candidateConcepts.length);
    for (CandidateConcept candidateConcept : candidateConcepts) {
      this.candidateConcepts.add(candidateConcept);
      candidateConceptIds.add(candidateConcept.conceptId);
    }
    if (!candidateConceptIds.containsAll(this.includedConcepts)) {
      throw new IllegalArgumentException("Not all included concepts are in the set of candidate concepts");
    }
  }

  /**
   * Encapsulates a concept ID and the exclude/descendant flags.
   */
  public class ConceptExpression {

    public long conceptId;
    public boolean exclude;
    public boolean descendants;

    public ConceptExpression(long conceptId, boolean exclude, boolean descendants) {
      this.conceptId = conceptId;
      this.exclude = exclude;
      this.descendants = descendants;
    }

    public void print() {
      System.out.println("Concept ID: " + conceptId + ", exclude: " + exclude + ", descendants: " + descendants);
    }
  }

  /**
   * Encapsulates the conceptId and concept's descendants and is used to capture
   * the options required for the final solution.
   */
  public static class CandidateConcept {

    public enum Options {
      INCLUDE, INCLUDE_WITH_DESCENDANTS, EXCLUDE, EXCLUDE_WITH_DESCENDANTS, IGNORE
    }

    public long conceptId;
    public Set<Long> descendants;
    public Options[] validOptions;
    public boolean inConceptSet;
    public int relevantDescendantCount;

    /**
     * @param conceptId The concept ID
     * @param descendants All descendant concept IDs. This follows OHDSI
     * convention that the descendants include the concept set itself, so the
     * minimum size is 1.
     */
    public CandidateConcept(long conceptId, long[] descendants) {
      this.conceptId = conceptId;
      this.descendants = new HashSet<>(descendants.length);
      for (long descendant : descendants) {
        this.descendants.add(descendant);
      }
      if (!this.descendants.contains(conceptId)) {
        throw new IllegalArgumentException("Descendants do not include concept itself for concept " + conceptId);
      }
    }

    public boolean hasValidOption(Options option) {
      for (Options validOption : validOptions) {
        if (validOption == option) {
          return true;
        }
      }
      return false;
    }
  }

  /**
   * Condense the concept set to the optimal (shortest) expression.
   */
  public void condense() {
    determineValidStatesAndRemoveRedundant();
    bruteForceSearch();
  }

  /**
   * Get the optimal concept set expression. Will throw an exception if
   * condense() hasn't been called first.
   *
   * @return An array of ConceptExpression
   */
  public ConceptExpression[] getConceptSetExpression() {
    if (optimalLength == -1) {
      throw new RuntimeException("Must run condense() first");
    }
    ConceptExpression[] expression = new ConceptExpression[optimalLength];
    int cursor = 0;
    for (int i = 0; i < optimalSolution.length; i++) {
      long conceptId = candidateConcepts.get(i).conceptId;
      switch (optimalSolution[i]) {
        case INCLUDE:
          expression[cursor++] = new ConceptExpression(conceptId, false, false);
          break;
        case INCLUDE_WITH_DESCENDANTS:
          expression[cursor++] = new ConceptExpression(conceptId, false, true);
          break;
        case EXCLUDE:
          expression[cursor++] = new ConceptExpression(conceptId, true, false);
          break;
        case EXCLUDE_WITH_DESCENDANTS:
          expression[cursor++] = new ConceptExpression(conceptId, true, true);
          break;
        case IGNORE:
          break;
      }
    }
    return expression;
  }

  private void bruteForceSearch() {
    // Sort candidate concepts to determine order in which tree is traversed.
    // First sorting so inclusions are executed first. This allows us to build the concept set 
    // on the fly (exclusions must always be done last).
    // Then sorting so inclusions that are not optional are done first. This just means they are 
    // not re-evaluated all the time
    // Finally sorting by the size of descendants. Since IGNORE is the first option
    // that is evaluated, this means in the first part of the tree search we will evaluate
    // options where these lower-level concepts are ignored.
    Comparator<CandidateConcept> comparator = (CandidateConcept obj1, CandidateConcept obj2) -> {
      int result = Boolean.compare(obj2.inConceptSet, obj1.inConceptSet);
      if (result != 0) {
        return result;
      }
      result = Boolean.compare(obj2.validOptions.length == 1, obj1.validOptions.length == 1);
      if (result != 0) {
        return result;
      }
      return Integer.compare(obj2.relevantDescendantCount, obj1.relevantDescendantCount);
    };
    candidateConcepts.sort(comparator);

    // At each level in the tree, determine what concepts could still be included or excluded from 
    // that point forward. This will help terminate a branch when it cannot achieve a valid solution:
    firstExclusionIndex = candidateConcepts.size();
    remainingConceptsAtLevel = new ArrayList<>(candidateConcepts.size());
    Set<Long> remainingConcepts = new HashSet<>();
    for (int i = candidateConcepts.size() - 1; i >= 0; i--) {
      CandidateConcept candidateConcept = candidateConcepts.get(i);
      if (candidateConcept.inConceptSet) {
        if (i == firstExclusionIndex - 1) {
          remainingConcepts.clear();
        }
        if (candidateConcept.hasValidOption(CandidateConcept.Options.INCLUDE_WITH_DESCENDANTS)) {
          remainingConcepts.addAll(candidateConcept.descendants);
        } else {
          remainingConcepts.add(candidateConcept.conceptId);
        }
      } else {
        firstExclusionIndex = i;
        if (candidateConcept.hasValidOption(CandidateConcept.Options.EXCLUDE_WITH_DESCENDANTS)) {
          remainingConcepts.addAll(candidateConcept.descendants);
        } else {
          remainingConcepts.add(candidateConcept.conceptId);
        }
      }
      remainingConceptsAtLevel.add(new HashSet<>(remainingConcepts));
    }
    Collections.reverse(remainingConceptsAtLevel);

    optimalLength = candidateConcepts.size() + 1;
    optimalSolution = new CandidateConcept.Options[candidateConcepts.size()];
    currentSolution = new CandidateConcept.Options[candidateConcepts.size()];
    stepCount = 0;
    recurseOverOptions(0, 0, new HashSet<>());
    if (stepCount >= MAX_STEP_COUNT) {
      System.out.println("Reached max step count. Solution may not be optimal");
    }
  }

  private void recurseOverOptions(int index, int currentLength, Set<Long> currentConceptSet) {
    stepCount++;
    if (stepCount > MAX_STEP_COUNT) {
      return;
    } else if (currentLength >= optimalLength) {
      // Already cannot improve on current best solution
      return;
    } else if (index == candidateConcepts.size()) {
      evaluateCurrentSolution(currentLength, currentConceptSet);
    } else if (index == firstExclusionIndex && !currentConceptSet.containsAll(includedConcepts)) {
      // Only exclusion options from here on, so if not all required concepts are in current
      // set there is no way to add them
      return;
    } else {
      if (index < firstExclusionIndex) {
        Set<Long> missingConcepts = new HashSet<>(includedConcepts);
        missingConcepts.removeAll(currentConceptSet);
        if (!remainingConceptsAtLevel.get(index).containsAll(missingConcepts)) {
          return;
        }
      } else {
        Set<Long> surplusConcepts = new HashSet<>(currentConceptSet);
        surplusConcepts.removeAll(includedConcepts);
        if (!remainingConceptsAtLevel.get(index).containsAll(surplusConcepts)) {
          return;
        }
      }
      CandidateConcept candidateConcept = candidateConcepts.get(index);
      for (CandidateConcept.Options option : candidateConcept.validOptions) {
        currentSolution[index] = option;
        Set<Long> newConceptSet;
        int newLength = currentLength;
        switch (option) {
          case INCLUDE:
            if (currentConceptSet.contains(candidateConcept.conceptId)) {
              continue;
            }
            newConceptSet = new HashSet<>(currentConceptSet);
            newConceptSet.add(candidateConcept.conceptId);
            newLength++;
            break;
          case INCLUDE_WITH_DESCENDANTS:
            if (currentConceptSet.containsAll(candidateConcept.descendants)) {
              continue;
            }
            Set<Long> surplusConcepts = new HashSet<>(candidateConcept.descendants);
            surplusConcepts.removeAll(currentConceptSet);
            if (Collections.disjoint(includedConcepts, surplusConcepts)) {
              continue;
            }
            newConceptSet = new HashSet<>(currentConceptSet);
            newConceptSet.addAll(candidateConcept.descendants);
            newLength++;
            break;
          case EXCLUDE:
            if (!currentConceptSet.contains(candidateConcept.conceptId)
              || includedConcepts.contains(candidateConcept.conceptId)) {
              continue;
            }
            newConceptSet = new HashSet<>(currentConceptSet);
            newConceptSet.remove(candidateConcept.conceptId);
            newLength++;
            break;
          case EXCLUDE_WITH_DESCENDANTS:
            if (Collections.disjoint(currentConceptSet, candidateConcept.descendants)) {
              continue;
            }
            newConceptSet = new HashSet<>(currentConceptSet);
            newConceptSet.removeAll(candidateConcept.descendants);
            newLength++;
            break;
          default:
            // IGNORE
            newConceptSet = currentConceptSet;
            break;
        }
        recurseOverOptions(index + 1, newLength, newConceptSet);
      }
    }
  }

  private void evaluateCurrentSolution(int currentLength, Set<Long> currentConceptSet) {
    if (currentConceptSet.equals(includedConcepts)) {
      if (currentLength < optimalLength) {
        optimalSolution = Arrays.copyOf(currentSolution, currentSolution.length);
        optimalLength = currentLength;
      }
    }
  }

  private void determineValidStatesAndRemoveRedundant() {
    Set<Long> candidatesToRemove = new HashSet<>();
    List<CandidateConcept.Options> validOptions = new ArrayList<>(5);
    for (CandidateConcept candidateConcept : candidateConcepts) {
      if (candidatesToRemove.contains(candidateConcept.conceptId)) {
        continue;
      }
      validOptions.clear();

      if (includedConcepts.contains(candidateConcept.conceptId)) {
        // Concept is in the concept set
        candidateConcept.inConceptSet = true;
        // If concept is not a descendant of any other concept, it is not an option to
        // ignore it
        if (!cantIgnore(candidateConcept)) {
          validOptions.add(CandidateConcept.Options.IGNORE);
        }
        if (candidateConcept.descendants.size() == 1) {
          // Has no descendants, so no difference between INCLUDE and
          // INCLUDE_WITH_DESCENDANTS
          validOptions.add(CandidateConcept.Options.INCLUDE_WITH_DESCENDANTS);
          candidateConcept.relevantDescendantCount = 1;
        } else {
          if (includedConcepts.containsAll(candidateConcept.descendants)) {
            // All descendants are in concept set, so no reason to evaluate
            // INCLUDE
            validOptions.add(CandidateConcept.Options.INCLUDE_WITH_DESCENDANTS);
            candidateConcept.relevantDescendantCount = candidateConcept.descendants.size();

            // Also, descendants are redundant, so can be removed from candidate list:
            Set<Long> toRemove = new HashSet<>(candidateConcept.descendants);
            toRemove.remove(candidateConcept.conceptId);
            candidatesToRemove.addAll(toRemove);
          } else {
            validOptions.add(CandidateConcept.Options.INCLUDE);
            validOptions.add(CandidateConcept.Options.INCLUDE_WITH_DESCENDANTS);

            Set<Long> intersection = new HashSet<>(candidateConcept.descendants);
            intersection.retainAll(includedConcepts);
            candidateConcept.relevantDescendantCount = intersection.size();
          }
        }
      } else {
        // Concept is *not* in the concept set
        candidateConcept.inConceptSet = false;
        candidateConcept.relevantDescendantCount = candidateConcept.descendants.size();
        validOptions.add(CandidateConcept.Options.IGNORE);
        if (candidateConcept.descendants.size() == 1) {
          // Has no descendants, so no difference between EXCLUDE and
          // EXCLUDE_WITH_DESCENDANTS
          validOptions.add(CandidateConcept.Options.EXCLUDE_WITH_DESCENDANTS);
        } else {
          validOptions.add(CandidateConcept.Options.EXCLUDE);
          if (Collections.disjoint(includedConcepts, candidateConcept.descendants)) {
            // None of the descendants are in concept set, so can EXCLUDE_WITH_DESCENDANTS 
            validOptions.add(CandidateConcept.Options.EXCLUDE_WITH_DESCENDANTS);

            // Also, descendants are redundant, so can be removed from candidate list:
            Set<Long> toRemove = new HashSet<>(candidateConcept.descendants);
            toRemove.remove(candidateConcept.conceptId);
            candidatesToRemove.addAll(toRemove);
          }
        }
      }
      candidateConcept.validOptions = validOptions.toArray(new CandidateConcept.Options[validOptions.size()]);
    }
    candidateConcepts.removeIf(candidateConcept -> candidatesToRemove.contains(candidateConcept.conceptId));
  }

  private boolean cantIgnore(CandidateConcept candidateConcept) {
    for (CandidateConcept otherCandidateConcept : candidateConcepts) {
      if (otherCandidateConcept != candidateConcept) {
        if (otherCandidateConcept.descendants.contains(candidateConcept.conceptId)) {
          return false;
        }
      }
    }
    return true;
  }
}
