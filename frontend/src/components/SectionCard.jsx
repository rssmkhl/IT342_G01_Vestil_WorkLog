import PropTypes from 'prop-types';

const SectionCard = ({ title, subtitle, action, children }) => (
  <section className="section-card">
    <div className="section-card__header">
      <div>
        <h3>{title}</h3>
        {subtitle ? <p>{subtitle}</p> : null}
      </div>
      {action ? <div>{action}</div> : null}
    </div>
    <div className="section-card__body">{children}</div>
  </section>
);

SectionCard.propTypes = {
  title: PropTypes.node.isRequired,
  subtitle: PropTypes.node,
  action: PropTypes.node,
  children: PropTypes.node,
};

export default SectionCard;
