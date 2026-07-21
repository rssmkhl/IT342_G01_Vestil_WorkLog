import PropTypes from 'prop-types';
import { BarChart } from 'lucide-react';

const MetricCard = ({ title, value, description, icon: Icon, accentClass, loading = false }) => {
  if (loading) {
    return (
      <div className={`metric-card ${accentClass || ''}`}>
        <div className="metric-card__skeleton" />
      </div>
    );
  }

  return (
    <div className={`metric-card ${accentClass || ''}`}>
      <div className="metric-card__icon">
        {Icon ? <Icon size={20} /> : <BarChart size={20} />}
      </div>
      <div>
        <p className="metric-card__title">{title}</p>
        <h3 className="metric-card__value">{value}</h3>
        <span className="metric-card__description">{description}</span>
      </div>
    </div>
  );
};

MetricCard.propTypes = {
  title: PropTypes.node.isRequired,
  value: PropTypes.node.isRequired,
  description: PropTypes.node,
  icon: PropTypes.elementType,
  accentClass: PropTypes.string,
  loading: PropTypes.bool,
};

export default MetricCard;
