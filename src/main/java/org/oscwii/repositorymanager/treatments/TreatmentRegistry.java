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
