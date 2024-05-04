/*
 * Copyright (c) 2023-2024 Open Shop Channel
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.oscwii.repositorymanager.treatments;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

@Component
public class TreatmentRegistry
{
    private final Map<String, TreatmentRunnable> treatments = new HashMap<>();

    public void registerTreatment(TreatmentRunnable treatment)
    {
        Assert.notNull(treatment, "Treatment cannot be null!");
        treatments.put(treatment.getId().toLowerCase(), treatment);
    }

    @Nullable
    public TreatmentRunnable getTreatment(String id)
    {
        return treatments.get(id.toLowerCase());
    }
}
