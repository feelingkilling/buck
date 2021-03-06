/*
 * Copyright 2018-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.facebook.buck.core.rules.knowntypes;

import com.facebook.buck.core.description.BaseDescription;
import com.facebook.buck.core.description.impl.DescriptionCache;
import com.facebook.buck.core.exceptions.HumanReadableException;
import com.facebook.buck.core.model.RuleType;
import com.facebook.buck.core.rules.config.KnownConfigurationRuleTypes;
import com.facebook.buck.core.util.immutables.BuckStyleImmutable;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.function.Function;
import java.util.stream.Stream;
import org.immutables.value.Value;

/** Provides access to rule types. */
@Value.Immutable(builder = false, copy = false)
@BuckStyleImmutable
public abstract class AbstractKnownRuleTypes {

  @Value.Parameter
  public abstract KnownBuildRuleTypes getKnownBuildRuleTypes();

  @Value.Parameter
  public abstract KnownConfigurationRuleTypes getKnownConfigurationRuleTypes();

  @Value.Lazy
  public ImmutableMap<String, RuleType> getTypesByName() {
    return Stream.concat(
            getKnownBuildRuleTypes().getDescriptions().stream(),
            getKnownConfigurationRuleTypes().getRuleDescriptions().stream())
        .map(DescriptionCache::getRuleType)
        .collect(ImmutableMap.toImmutableMap(RuleType::getName, t -> t));
  }

  /**
   * @param name user-facing name of a rule, e.g. "java_library"
   * @return {@link RuleType} that corresponds to the provided name.
   */
  public RuleType getRuleType(String name) {
    RuleType type = getTypesByName().get(name);
    if (type == null) {
      throw new HumanReadableException("Unable to find rule type: %s", name);
    }
    return type;
  }

  /** @return all known descriptions */
  @Value.Lazy
  public ImmutableList<BaseDescription<?>> getDescriptions() {
    return ImmutableList.<BaseDescription<?>>builder()
        .addAll(getKnownBuildRuleTypes().getDescriptions())
        .addAll(getKnownConfigurationRuleTypes().getRuleDescriptions())
        .build();
  }

  /** @return all descriptions organized by their {@link RuleType}. */
  @Value.Lazy
  protected ImmutableMap<RuleType, BaseDescription<?>> getDescriptionsByRule() {
    return Stream.concat(
            getKnownBuildRuleTypes().getDescriptions().stream(),
            getKnownConfigurationRuleTypes().getRuleDescriptions().stream())
        .collect(ImmutableMap.toImmutableMap(DescriptionCache::getRuleType, Function.identity()));
  }

  /** @return a description by its {@link RuleType}. */
  public BaseDescription<?> getDescription(RuleType ruleType) {
    return Preconditions.checkNotNull(
        getDescriptionsByRule().get(ruleType), "Cannot find a description for type %s", ruleType);
  }
}
