/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.minsait.onesait.platform.scheduler.scheduler.generator.impl;

import java.util.Date;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.scheduler.scheduler.generator.TriggerGenerator;

@Service
public class TriggerGeneratorImpl implements TriggerGenerator{
	
	@Override
	public Trigger createTrigger(JobDetail jobDetail, TriggerKey triggerKey, Date startAt, Date endAt) {
		
		TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(triggerKey)
                .startNow();
		
		if (startAt != null) {
			triggerBuilder.startAt(startAt);
        }        
        
        if (endAt != null) {
        	triggerBuilder.endAt(endAt);
        }
        
        return triggerBuilder.build();
	}

	@Override
	public Trigger createCronTrigger(String cronExpression, JobDetail jobDetail, TriggerKey triggerKey, Date startAt, Date endAt) {
		
		CronScheduleBuilder schedBuilder = CronScheduleBuilder.cronSchedule(cronExpression).
				   withMisfireHandlingInstructionDoNothing();
        
		TriggerBuilder<CronTrigger> builder = TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withSchedule(schedBuilder)
                .withIdentity(triggerKey);
                
        if (startAt != null) {
        	builder.startAt(startAt);
        }        
        
        if (endAt != null) {
        	builder.endAt(endAt);
        }
        
        return builder.build();
    }
	
}
