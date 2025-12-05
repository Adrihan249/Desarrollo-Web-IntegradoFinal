// src/components/activity/ActivityTimeline.jsx
import { useState, useEffect } from 'react';
import { Calendar } from 'lucide-react';
import ActivityItem from './ActivityItem';
import ActivityFilters from './ActivityFilters';
import EmptyState from '../common/EmptyState';
import Spinner from '../common/Spinner';
import activityService from '../../services/activityService';
import { formatDate } from '../../utils/formatters';

const ActivityTimeline = ({ projectId }) => {
  const [activities, setActivities] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [filters, setFilters] = useState({
    actionType: 'all',
    userId: null,
    startDate: null,
    endDate: null
  });

  useEffect(() => {
    loadActivities();
  }, [projectId, filters]);

  const loadActivities = async () => {
    try {
      const data = await activityService.getTimeline(projectId, filters);
      setActivities(data);
    } catch (error) {
      console.error('Error loading activities:', error);
    } finally {
      setIsLoading(false);
    }
  };

  // Group activities by date
  const groupedActivities = activities.reduce((acc, activity) => {
    const date = new Date(activity.createdAt).toLocaleDateString();
    if (!acc[date]) {
      acc[date] = [];
    }
    acc[date].push(activity);
    return acc;
  }, {});

  if (isLoading) {
    return (
      <div className="flex justify-center py-8">
        <Spinner />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Filters */}
      <ActivityFilters
        filters={filters}
        onChange={setFilters}
        projectId={projectId}
      />

      {/* Timeline */}
      {Object.keys(groupedActivities).length === 0 ? (
        <EmptyState
          icon={<Calendar className="w-12 h-12" />}
          title="No hay actividades"
          description="Las actividades del proyecto aparecerán aquí"
        />
      ) : (
        <div className="space-y-8">
          {Object.entries(groupedActivities).map(([date, dateActivities]) => (
            <div key={date}>
              {/* Date Header */}
              <div className="flex items-center gap-3 mb-4">
                <h3 className="text-sm font-semibold text-gray-900">
                  {formatDate(dateActivities[0].createdAt, 'long')}
                </h3>
                <div className="flex-1 h-px bg-gray-200" />
              </div>

              {/* Activities */}
              <div className="relative pl-8 space-y-4">
                {/* Timeline line */}
                <div className="absolute left-2 top-2 bottom-2 w-0.5 bg-gray-200" />

                {dateActivities.map((activity, index) => (
                  <ActivityItem
                    key={activity.id}
                    activity={activity}
                    isLast={index === dateActivities.length - 1}
                  />
                ))}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default ActivityTimeline;